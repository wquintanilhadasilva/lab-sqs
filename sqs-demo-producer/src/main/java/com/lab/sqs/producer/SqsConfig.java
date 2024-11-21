package com.lab.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.operations.TemplateAcknowledgementMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SqsConfig {

    @Value("${application.sqs-uri}")
    private String sqsUri;
    @Value("${application.sqs-region}")
    private String sqsRegion;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(sqsUri))
                .region(Region.of(sqsRegion))
                .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(ObjectMapper objectMapper) {

        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient())
                .configureDefaultConverter(converter -> {
                    converter.setObjectMapper(objectMapper);
                })
                .configure(options -> options
                        .acknowledgementMode(TemplateAcknowledgementMode.MANUAL))
                .build();

    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Adiciona o módulo para suporte ao Java 8 Time
        mapper.registerModule(new JavaTimeModule());
        // Configuração para evitar erros de serialização com datas no formato ISO
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

}
