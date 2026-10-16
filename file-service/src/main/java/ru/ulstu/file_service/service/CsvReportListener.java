package ru.ulstu.file_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import ru.ulstu.file_service.config.JmsConfig;
import ru.ulstu.file_service.domain.StoredFileInfo;

import java.util.Base64;

@Component
public class CsvReportListener {

    private static final Logger log = LoggerFactory.getLogger(CsvReportListener.class);

    private final FileStorageService storageService;
    private final SagaEventPublisher eventPublisher;

    public CsvReportListener(FileStorageService storageService,
                             SagaEventPublisher eventPublisher) {
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
    }

    @JmsListener(destination = JmsConfig.CSV_QUEUE)
    public void onCsvReport(CsvReportMessage message) {
        log.info("QUEUE <- received file={} base64len={}",
                message.filename(), message.contentBase64().length());

        try {
            if (message.filename().startsWith("fail-")) {
                throw new RuntimeException(
                        "Симуляция: файловое хранилище недоступно для " + message.filename());
            }

            byte[] bytes = Base64.getDecoder().decode(message.contentBase64());
            StoredFileInfo info = storageService.store(message.filename(), bytes);
            log.info("QUEUE: stored id={} name={} size={}",
                    info.id(), info.originalName(), info.size());

        } catch (Exception e) {
            log.error("QUEUE: failure для {}: {}", message.filename(), e.getMessage());

            eventPublisher.publishFailure(new FileSaveFailedEvent(
                    message.filename(),
                    message.contentBase64(),
                    e.getMessage()));
        }
    }
}