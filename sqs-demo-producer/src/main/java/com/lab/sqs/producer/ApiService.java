package com.lab.sqs.producer;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiService {

    private final SqsTemplate sqsTemplate;

    @Value("${application.sqs-queue-url}")
    private String queueUrl;

    public SendResult send(String message) {
        log.info("Recebendo mensagem para enviar: [{}]", message);
        SendMessage msg = new SendMessage(
                UUID.randomUUID().toString(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                message,
                0
        );
        log.info("Enviando mensagem [{}] para o SQS", msg);
        sqsTemplate.sendAsync(queueUrl, msg);
        return new SendResult(msg.id(), "Mensagem enviada para a fila SQS com sucesso!");
    }

}
