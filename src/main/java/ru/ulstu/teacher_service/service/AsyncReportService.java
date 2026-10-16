package ru.ulstu.teacher_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.ulstu.teacher_service.web.dto.StoredFileInfoDto;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class AsyncReportService {

    private static final Logger log = LoggerFactory.getLogger(AsyncReportService.class);

    private final RestTemplate restTemplate;
    private final FileStorageAsyncClient fileStorageAsyncClient;
    private final String reportServiceUrl;
    private final Executor executor;

    public AsyncReportService(RestTemplate restTemplate,
                              FileStorageAsyncClient fileStorageAsyncClient,
                              @Value("${report.service.url}") String reportServiceUrl,
                              @Qualifier("reportTaskExecutor") Executor executor) {
        this.restTemplate = restTemplate;
        this.fileStorageAsyncClient = fileStorageAsyncClient;
        this.reportServiceUrl = reportServiceUrl;
        this.executor = executor;
    }

    public CompletableFuture<StoredFileInfoDto> saveCsvAndWaitInStorage() {
        return CompletableFuture.supplyAsync(() -> {
                    String url = reportServiceUrl + "/api/reports/teachers/csv/save";
                    log.info("ASYNC -> report-service: POST {}", url);

                    ResponseEntity<Map> response =
                            restTemplate.postForEntity(url, null, Map.class);

                    Map body = response.getBody();
                    if (body == null || body.get("filename") == null) {
                        throw new IllegalStateException(
                                "report-service не вернул имя файла");
                    }

                    String filename = String.valueOf(body.get("filename"));
                    log.info("ASYNC <- report-service: filename={}", filename);
                    return filename;
                }, executor)
                .thenCompose(fileStorageAsyncClient::waitForFileAsync);
    }

    public CompletableFuture<DownloadedReport> downloadLatestCsvWithInfo() {
        return fileStorageAsyncClient.findLatestCsvAsync()
                .thenCompose(info -> fileStorageAsyncClient.downloadAsync(info.id())
                        .thenApply(bytes -> new DownloadedReport(info, bytes)));
    }

    public record DownloadedReport(StoredFileInfoDto info, byte[] content) {
    }
}