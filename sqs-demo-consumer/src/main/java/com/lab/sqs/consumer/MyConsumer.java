package com.lab.sqs.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MyConsumer {

    private final RestTemplate restTemplate;
    private final SqsTemplate sqsTemplate;

    @Value("${application.sqs-retry-queue-url}")
    private String retryQueueUrl;

    @Value("${application.sqs-response-queue-url}")
    private String responseQueueUrl;

    @Value("${application.external-uri}")
    private String externalUri;

    @Value("${application.sqs-retry-delay-seconds}")
    private Integer delay;

    @SqsListener("${application.sqs-queue-name}")
    public void listen(Message<ReceiveMessage> message) {
        log.info("Recebendo a mensagem [{}]", message);
        ReceiveMessage msg = message.getPayload();
        log.info("Enviando a mensagem para o receiver [{}]", msg);
        try {
            String result = sendToExternal(msg);
            log.info("Resultado do envio: [{}]", result);
            Acknowledgement.acknowledge(message); // Faz o ack da mensagem enviada para o receiver
        } catch (Exception e) {
            log.error("Erro ao enviar para o serviço externo, deve executar um retry", e);
            try {
                sendToRetry(msg);
                Acknowledgement.acknowledge(message); // Faz o ack da mensagem já colocada na fila de retry
            } catch (Exception ex) {
                log.error("Erro ao reenfileirar a mensagem [{}]", msg);
                throw ex;
            }
        }
    }

    @SqsListener("${application.sqs-retry-queue-name}")
    public void listenRetry(Message<ReceiveMessage> message) {
        log.info("Recebendo a mensagem [{}] na retentativa [{}]", message.getPayload().id(), message.getPayload().equeueCount());
        ReceiveMessage msg = message.getPayload();
        log.info("Tentando reenviar a mensagem [{}] para o receiver", msg.id());
        try {
            String result = sendToExternal(msg);
            log.info("Resultado do reenvio: [{}]", result);
            Acknowledgement.acknowledge(message);
        } catch (Exception e) {
            log.error("Erro ao reenviar para o serviço externo [{}], pela [{}] vez, deve executar um retry", msg.id(), msg.equeueCount(), e);
            try {
                sendToRetry(msg);
                Acknowledgement.acknowledge(message); // Faz o ack da mensagem já colocada na fila de retry
            } catch (Exception ex) {
                log.error("Erro ao reenfileirar a mensagem [{}] pela [{}] vez", msg.id(), msg.equeueCount());
                throw ex;
            }
        }
    }

    private String sendToExternal(ReceiveMessage receiveMessage) {
        var result = restTemplate.postForObject(externalUri + "/process", receiveMessage, String.class);
        log.info("Comunicação com externo com sucesso, enfileirando resultado processado para [{}] com [{}] tentativas",
                receiveMessage.id(), receiveMessage.equeueCount());
        var r = queue(result, responseQueueUrl, 0 );
        log.info("Enviado resultado processado [{}]", r);
        return result;
    }

    private void sendToRetry(ReceiveMessage msg) {
        // Incrementa o número de retentativas
        ReceiveMessage newMsg =  msg.enqueueIncrement();
        log.info("Reenfileirando [{}] para tentar novamente em [{}] segundos pela [{}] vez", msg.id(), delay, msg.equeueCount());
        var result = queue(newMsg, retryQueueUrl, delay);
        log.info("Enviado para a retentaiva[{}]", result);
    }

    private <T,R> R queue(T msg, String queueName, Integer delay) {
        var result = sqsTemplate.send(to -> to.queue(queueName)
                .payload(msg)
                .headers(Map.of("H1Delay", "VH1DelayValue", "H2Delay", "VH2DelayValue"))
                .delaySeconds(delay)
        );
        log.info("Mensagem [{}] publicada em [{}] com [{}] delay", msg, queueName, delay);
        return (R) result;
    }

}
