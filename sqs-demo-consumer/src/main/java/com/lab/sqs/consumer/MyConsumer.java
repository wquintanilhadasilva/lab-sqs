package com.lab.sqs.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class MyConsumer {

    private final RestTemplate restTemplate;

    @Value("${application.external-uri}")
    private String externalUri;

    @SqsListener("${application.sqs-queue-name}")
    public void listen(Message<ReceiveMessage> message) {
        log.info("Recebendo a mensagem [{}]", message);
        ReceiveMessage msg = message.getPayload();
        log.info("Enviando a mensagem para o receiver [{}]", msg);
        // TODO
        try {
            String result = sendToExternal(msg);
            log.info("Resultado do envio: [{}]", result);
        } catch (Exception e) {
            log.error("Erro ao enviar para o serviço externo", e);
        }
    }

    private String sendToExternal(ReceiveMessage receiveMessage) {
        return restTemplate.postForObject(externalUri + "/process", receiveMessage, String.class);
    }

}
