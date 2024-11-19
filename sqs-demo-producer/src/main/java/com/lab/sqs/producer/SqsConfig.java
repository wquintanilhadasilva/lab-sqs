package com.lab.sqs.producer;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.support.converter.AbstractMessagingMessageConverter;
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
    public SqsTemplate sqsTemplate(SqsAsyncClient asyncClient) {
        return SqsTemplate.builder()
                .sqsAsyncClient(asyncClient)
                .configureDefaultConverter(AbstractMessagingMessageConverter::doNotSendPayloadTypeHeader)
                .build();
    }

}
