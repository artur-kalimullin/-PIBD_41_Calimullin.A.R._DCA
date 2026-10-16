package ru.ulstu.file_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import ru.ulstu.file_service.service.CsvReportMessage;
import ru.ulstu.file_service.service.FileSaveFailedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

    public static final String CSV_QUEUE          = "report.csv.queue";
    public static final String COMPENSATION_QUEUE = "saga.compensation.queue";

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("csvReport",           CsvReportMessage.class);
        typeIdMappings.put("fileSaveFailedEvent", FileSaveFailedEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}