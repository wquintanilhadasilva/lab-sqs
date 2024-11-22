package com.lab.sqs.consumer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

import java.util.Map;
import java.util.Optional;
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

    public String createQueue(String queueName, Map<QueueAttributeName,String> attributes) throws ExecutionException, InterruptedException {
        log.info("Verificando se a fila [{}] existe...", queueName);
        return checkExistQueue(queueName).orElseGet(() -> create(queueName, attributes));
    }

    public String createQueue(String queueName) throws ExecutionException, InterruptedException {
        return createQueue(queueName, null);
    }

    private String getQueueArn(String queueUrl) throws ExecutionException, InterruptedException {
        GetQueueAttributesResponse attributesResponse = sqsAsyncClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()).get();

        return attributesResponse.attributes().get(QueueAttributeName.QUEUE_ARN);
    }

    private void createQueueWithRedrivePolicy(String queueName, String dlqArn) throws ExecutionException, InterruptedException {
        checkExistQueue(queueName).ifPresentOrElse(uri -> log.warn("Fila [{}] já existe e não será criada!", queueName),
                () -> {
                    log.info("Criando fila [{}] com DLQ associada [{}]", queueName, dlqArn);
                    String atr = String.format("{\"deadLetterTargetArn\":\"%s\",\"maxReceiveCount\":\"%s\"}", dlqArn, maxRetryCount);
                    Map<QueueAttributeName, String> attributes = Map.of(QueueAttributeName.REDRIVE_POLICY,atr);
                    try {
                        String queueUrl = createQueue(queueName, attributes);
                        log.info("Fila [{}] criada com sucesso e associada à DLQ [{}]. URL: [{}]",
                                queueName, dlqArn, queueUrl);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private String create(String queueName, Map<QueueAttributeName,String> attributes) {
        log.info("Criando a fila [{}] com os atributos [{}].", queueName, attributes);
        CreateQueueResponse createQueueResponse = null;
        try {
            createQueueResponse = sqsAsyncClient.createQueue(CreateQueueRequest.builder()
                    .queueName(queueName)
                    .attributes(attributes)
                    .build()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info("Fila [{}] criada com sucesso. URL: [{}]", queueName, createQueueResponse.queueUrl());
        return createQueueResponse.queueUrl();
    }

    private Optional<String> checkExistQueue(String queueName) throws ExecutionException, InterruptedException {
        try {
            GetQueueUrlResponse response = sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).get();
            log.info("Fila [{}] já existe com URL [{}]", queueName, response.queueUrl());
            return Optional.of(response.queueUrl());
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof QueueDoesNotExistException) {
                log.warn("Fila [{}] não encontrada.", queueName);
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

}
