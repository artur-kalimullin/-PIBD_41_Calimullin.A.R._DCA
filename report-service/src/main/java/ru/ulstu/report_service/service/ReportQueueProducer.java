package ru.ulstu.report_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import ru.ulstu.report_service.config.JmsConfig;

@Component
public class ReportQueueProducer {

    private static final Logger log = LoggerFactory.getLogger(ReportQueueProducer.class);

    private final JmsTemplate jmsTemplate;

    public ReportQueueProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void send(CsvReportMessage message) {
        log.info("QUEUE -> {} file={} size={}",
                JmsConfig.CSV_QUEUE,
                message.filename(),
                message.contentBase64().length());
        jmsTemplate.convertAndSend(JmsConfig.CSV_QUEUE, message);
        log.info("QUEUE: message sent");
    }
}