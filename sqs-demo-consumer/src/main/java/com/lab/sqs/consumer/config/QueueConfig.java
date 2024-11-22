package com.lab.sqs.consumer.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.concurrent.ExecutionException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class QueueConfig {

    private final SqsAsyncClient asyncClient;

    @Value("${application.sqs-queue-name}")
    private String queueSentName;

    @Value("${application.sqs-retry-queue-name}")
    private String queueRetryName;

    @Value("${application.sqs-response-queue-name}")
    private String queueResponseName;

    @PostConstruct
    public void postConstruct() throws ExecutionException, InterruptedException {
        log.info("Criando as filas caso não existam....");
        QueueManagement manager = new QueueManagement(asyncClient, 5);
        manager.createQueueWithDlq(queueSentName, queueSentName.replace(".fifo", "") + "-dlq.fifo", Boolean.TRUE);
        manager.createQueueWithDlq(queueRetryName, queueRetryName + "-dlq", Boolean.FALSE);
        manager.createQueue(queueResponseName, Boolean.FALSE);
    }

}
