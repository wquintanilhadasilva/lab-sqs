package com.lab.sqs.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
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
    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(ObjectMapper objectMapper, SqsAsyncClient sqsAsyncClient) {
        // Usado para ler
        SqsMessagingMessageConverter messageConverter = new SqsMessagingMessageConverter();
        messageConverter.setPayloadTypeMapper(m -> null);
        messageConverter.setObjectMapper(objectMapper);

        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options ->
                        options.messageConverter(messageConverter)
                        .acknowledgementMode(AcknowledgementMode.MANUAL)
                )
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(ObjectMapper objectMapper) {
        // Usado para publicar
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient())
                .configureDefaultConverter(converter -> {
                    converter.setObjectMapper(objectMapper);
                })
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
