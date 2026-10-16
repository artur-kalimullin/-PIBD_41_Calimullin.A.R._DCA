package ru.ulstu.report_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import ru.ulstu.report_service.config.JmsConfig;

@Component
public class SagaCompensationListener {

    private static final Logger log = LoggerFactory.getLogger(SagaCompensationListener.class);

    private final ReportQueueProducer queueProducer;

    public SagaCompensationListener(ReportQueueProducer queueProducer) {
        this.queueProducer = queueProducer;
    }

    @JmsListener(destination = JmsConfig.COMPENSATION_QUEUE)
    public void onFileSaveFailed(FileSaveFailedEvent event) {
        log.warn("SAGA COMPENSATION <- получено событие: file={} reason={}",
                event.filename(), event.reason());

        log.warn("SAGA COMPENSATION: отчёт {} помечен как несохранённый",
                event.filename());

        if (event.filename().startsWith("retry-")) {
            log.error("SAGA COMPENSATION: файл {} уже был retry — повторная попытка не выполняется",
                    event.filename());
            return;
        }

        String retryFilename = "retry-" + event.filename().replace("fail-", "");
        CsvReportMessage retry = new CsvReportMessage(
                retryFilename,
                event.contentBase64());

        queueProducer.send(retry);
        log.info("SAGA COMPENSATION: повторная отправка выполнена: {}", retryFilename);
    }
}