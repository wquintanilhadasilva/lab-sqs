package com.lab.sqs.consumer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
@Slf4j
@RequiredArgsConstructor
public class QueueManagement {

    private final SqsAsyncClient sqsAsyncClient;
    private final int maxRetryCount;

    public void createQueueWithDlq(String queueName, String dlqName) {
        try {
            // Cria a DLQ
            String dlqUrl = createQueue(dlqName);
            // Configura a DLQ
            String dlqArn = getQueueArn(dlqUrl);

            // Cria a fila principal com a DLQ associada
            createQueueWithRedrivePolicy(queueName, dlqArn);
        } catch (Exception e) {
            log.error("Erro ao criar a fila ou associar a DLQ", e);
        }
    }

    private String createQueue(String queueName) throws ExecutionException, InterruptedException {
        log.info("Verificando se a fila [{}] existe...", queueName);

        try {
            GetQueueUrlResponse response = sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).get();
            log.info("Fila [{}] já existe com URL [{}]", queueName, response.queueUrl());
            return response.queueUrl();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof QueueDoesNotExistException) {
                log.info("Fila [{}] não encontrada. Criando uma nova.", queueName);

                CreateQueueResponse createQueueResponse = sqsAsyncClient.createQueue(CreateQueueRequest.builder()
                        .queueName(queueName)
                        .build()).get();

                log.info("Fila [{}] criada com sucesso. URL: [{}]", queueName, createQueueResponse.queueUrl());
                return createQueueResponse.queueUrl();
            } else {
                throw e;
            }
        }
    }

    private String getQueueArn(String queueUrl) throws ExecutionException, InterruptedException {
        GetQueueAttributesResponse attributesResponse = sqsAsyncClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()).get();

        return attributesResponse.attributes().get(QueueAttributeName.QUEUE_ARN);
    }

    private void createQueueWithRedrivePolicy(String queueName, String dlqArn) throws ExecutionException, InterruptedException {
        log.info("Criando fila [{}] com DLQ associada [{}]", queueName, dlqArn);

        String queueUrl = sqsAsyncClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .attributes(Map.of(
                        QueueAttributeName.REDRIVE_POLICY, String.format(
                                "{\"deadLetterTargetArn\":\"%s\",\"maxReceiveCount\":\"%s\"}", dlqArn, String.valueOf(maxRetryCount))
                ))
                .build()).get().queueUrl();

        log.info("Fila [{}] criada com sucesso e associada à DLQ [{}]. URL: [{}]", queueName, dlqArn, queueUrl);
    }

}
