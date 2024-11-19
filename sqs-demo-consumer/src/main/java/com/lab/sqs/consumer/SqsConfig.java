package com.lab.sqs.consumer;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
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

//    @Bean
//    public MappingJackson2MessageConverter customMessageConverter(ObjectMapper objectMapper) {
//        return new CustomMessageConverter(objectMapper);
//    }

//    @Bean
//    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
//        SqsMessagingMessageConverter messageConverter = new SqsMessagingMessageConverter();
//        messageConverter.setPayloadTypeMapper(m -> null);
//
//        return SqsMessageListenerContainerFactory
//                .builder()
//                .configure(options -> options.messageConverter(messageConverter))
//                .sqsAsyncClient(sqsAsyncClient)
//                .build();
//    }


}
