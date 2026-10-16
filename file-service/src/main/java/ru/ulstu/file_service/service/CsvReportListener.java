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

    public CsvReportListener(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @JmsListener(destination = JmsConfig.CSV_QUEUE)
    public void onCsvReport(CsvReportMessage message) {
        log.info("QUEUE <- received file={} base64len={}",
                message.filename(), message.contentBase64().length());
        try {
            byte[] bytes = Base64.getDecoder().decode(message.contentBase64());
            StoredFileInfo info = storageService.store(message.filename(), bytes);
            log.info("QUEUE: stored id={} name={} size={}",
                    info.id(), info.originalName(), info.size());
        } catch (Exception e) {
            log.error("QUEUE: failed to store file {}: {}",
                    message.filename(), e.getMessage(), e);
            throw e;
        }
    }
}