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
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
@Slf4j
@RequiredArgsConstructor
public class QueueManagement {

    private final SqsAsyncClient sqsAsyncClient;
    private final int maxRetryCount;

    public void createQueueWithDlq(String queueName, String dlqName, Boolean fifo) {
        try {
            // Cria a DLQ
            String dlqUrl = createQueue(dlqName, fifo);
            // Configura a DLQ
            String dlqArn = getQueueArn(dlqUrl);

            // Cria a fila principal com a DLQ associada e demais atributos de controle
            createQueueWithRedrivePolicy(queueName, dlqArn, fifo);
        } catch (Exception e) {
            log.error("Erro ao criar a fila ou associar a DLQ", e);
        }
    }

    public String createQueue(String queueName, Boolean fifo, Map<QueueAttributeName,String> attributes) throws ExecutionException, InterruptedException {
        log.info("Verificando se a fila [{}] existe...", queueName);
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        Map<QueueAttributeName, String> finalAttributes = new HashMap<>(attributes);
        if (fifo) {
            finalAttributes.put( QueueAttributeName.FIFO_QUEUE, fifo.toString());
        }
        return checkExistQueue(queueName).orElseGet(() -> create(queueName, finalAttributes));
    }

    public String createQueue(String queueName, Boolean fifo) throws ExecutionException, InterruptedException {
        Map<QueueAttributeName, String> attributes = buildAttributes(fifo);
        return createQueue(queueName, fifo, attributes);
    }

    private String getQueueArn(String queueUrl) throws ExecutionException, InterruptedException {
        GetQueueAttributesResponse attributesResponse = sqsAsyncClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()).get();

        return attributesResponse.attributes().get(QueueAttributeName.QUEUE_ARN);
    }

    private void createQueueWithRedrivePolicy(String queueName, String dlqArn, Boolean fifo) throws ExecutionException, InterruptedException {
        checkExistQueue(queueName).ifPresentOrElse(uri -> log.warn("Fila [{}] já existe e não será criada!", queueName),
                () -> {
                    log.info("Criando fila [{}] com DLQ associada [{}]", queueName, dlqArn);
                    String atr = String.format("{\"deadLetterTargetArn\":\"%s\",\"maxReceiveCount\":\"%s\"}", dlqArn, maxRetryCount);
                    Map<QueueAttributeName, String> attributes = buildAttributes(fifo);
                    attributes.put(QueueAttributeName.REDRIVE_POLICY, atr);
                    try {
                        String queueUrl = createQueue(queueName, fifo, attributes);
                        log.info("Fila [{}] criada com sucesso e associada à DLQ [{}]. URL: [{}]",
                                queueName, dlqArn, queueUrl);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private Map<QueueAttributeName,String> buildAttributes(Boolean fifo) {
        Map<QueueAttributeName,String> attr = new HashMap<>(Map.of(
                QueueAttributeName.MESSAGE_RETENTION_PERIOD, "86400", //em minutos = 1 dia
                QueueAttributeName.VISIBILITY_TIMEOUT, "4" // aguarda 20 seg para retornar à fila caso não confirmada
        ));
        if (fifo) {
            attr.put(QueueAttributeName.FIFO_QUEUE, fifo.toString());
            attr.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
        }
        return attr;
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

    private void setQueueAttributes(String queueName, Map<QueueAttributeName,String> attributes) {
        SetQueueAttributesRequest queueAttributesRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueName)
                .attributes(attributes)
                .build();
        sqsAsyncClient.setQueueAttributes(queueAttributesRequest);
    }

}
