package ru.ulstu.teacher_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.ulstu.teacher_service.web.dto.StoredFileInfoDto;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class FileStorageAsyncClient {

    private static final Logger log = LoggerFactory.getLogger(FileStorageAsyncClient.class);

    private final RestTemplate restTemplate;
    private final String fileServiceUrl;
    private final Executor executor;

    public FileStorageAsyncClient(RestTemplate restTemplate,
                                  @Value("${file.service.url}") String fileServiceUrl,
                                  @Qualifier("reportTaskExecutor") Executor executor) {
        this.restTemplate = restTemplate;
        this.fileServiceUrl = fileServiceUrl;
        this.executor = executor;
    }

    public CompletableFuture<List<StoredFileInfoDto>> listFilesAsync() {
        return CompletableFuture.supplyAsync(this::listFiles, executor);
    }

    private List<StoredFileInfoDto> listFiles() {
        String url = fileServiceUrl + "/api/files";
        log.info("ASYNC -> file-service: GET {}", url);

        ResponseEntity<StoredFileInfoDto[]> response =
                restTemplate.getForEntity(url, StoredFileInfoDto[].class);

        StoredFileInfoDto[] body = response.getBody();
        return body == null ? List.of() : Arrays.asList(body);
    }

    public CompletableFuture<StoredFileInfoDto> findLatestCsvAsync() {
        return listFilesAsync()
                .thenApply(files -> files.stream()
                        .filter(f -> f.originalName() != null
                                && f.originalName().toLowerCase().endsWith(".csv"))
                        .max(Comparator.comparing(StoredFileInfoDto::storedAt))
                        .orElseThrow(() -> new NotFoundException(
                                "В файловом хранилище нет CSV-отчётов")));
    }

    public CompletableFuture<byte[]> downloadAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            String url = fileServiceUrl + "/api/files/" + id;
            log.info("ASYNC -> file-service: GET {}", url);

            ResponseEntity<byte[]> response =
                    restTemplate.getForEntity(url, byte[].class);

            byte[] body = response.getBody();
            if (body == null) {
                throw new NotFoundException("Файл " + id + " пуст");
            }
            return body;
        }, executor);
    }

    public CompletableFuture<StoredFileInfoDto> waitForFileAsync(String originalName) {
        return CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 30; i++) {
                try {
                    Optional<StoredFileInfoDto> found = listFiles().stream()
                            .filter(f -> originalName.equals(f.originalName()))
                            .findFirst();

                    if (found.isPresent()) {
                        log.info("ASYNC: файл {} найден в хранилище, id={}",
                                originalName, found.get().id());
                        return found.get();
                    }

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
            }

            throw new CompletionException(new NotFoundException(
                    "Файл " + originalName + " не появился в хранилище за 15 секунд"));
        }, executor);
    }
}