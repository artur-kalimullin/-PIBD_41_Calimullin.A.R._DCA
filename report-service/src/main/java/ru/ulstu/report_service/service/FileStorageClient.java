package ru.ulstu.report_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Component
public class FileStorageClient {

    private static final Logger log = LoggerFactory.getLogger(FileStorageClient.class);

    private final RestTemplate restTemplate;
    private final String fileServiceUrl;

    public FileStorageClient(RestTemplate restTemplate,
                             @Value("${file.service.url}") String fileServiceUrl) {
        this.restTemplate = restTemplate;
        this.fileServiceUrl = fileServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> upload(String filename, byte[] content) {
        String url = fileServiceUrl + "/api/files";
        Map<String, Object> body = Map.of(
                "filename", filename,
                "content", Base64.getEncoder().encodeToString(content)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("UPLOAD -> file-service: POST {} name={} size={}", url, filename, content.length);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        log.info("UPLOAD <- file-service: status={} body={}",
                response.getStatusCode().value(), response.getBody());
        return response.getBody();
    }
}