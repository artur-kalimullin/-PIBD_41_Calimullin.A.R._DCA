package ru.ulstu.file_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import ru.ulstu.file_service.config.JmsConfig;

@Component
public class SagaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SagaEventPublisher.class);

    private final JmsTemplate jmsTemplate;

    public SagaEventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishFailure(FileSaveFailedEvent event) {
        jmsTemplate.convertAndSend(JmsConfig.COMPENSATION_QUEUE, event);
        log.warn("SAGA <- отправлено событие о сбое в {}: file={} reason={}",
                JmsConfig.COMPENSATION_QUEUE, event.filename(), event.reason());
    }
}