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

    @Value("${application.workflow-uri}")
    private String workflowUri;

    @Value("${application.sqs-retry-delay-seconds}")
    private Integer delay;

    @SqsListener(value = "${application.sqs-queue-name}", maxConcurrentMessages = "5", maxMessagesPerPoll = "3")
    public void listen(Message<ReceiveMessage> message) {
        log.info("Recebendo a mensagem [{}]", message);
        ReceiveMessage msg = message.getPayload();
        log.info("Enviando a mensagem para o receiver [{}]", msg);
        try {
            String result = sendToExternal(msg);
            log.info("Resultado do envio: [{}]", result);
            sentToFlow(result);
            Acknowledgement.acknowledge(message); // Faz o ack da mensagem enviada para o receiver
        } catch (Exception e) {
            log.error("Erro ao enviar para o serviço externo, deve executar um retry", e);
            try {
                sendToRetry(msg);
                Acknowledgement.acknowledge(message); // Faz o ack da mensagem já colocada na fila de retry
            } catch (Exception ex) {
                log.error("Erro ao enviar a mensagem para a fila de retry [{}]", msg);
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
            sentToFlow(result);
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
        var result = sentRestApi(receiveMessage, externalUri + "/process");
        log.info("Comunicação com externo com sucesso, enfileirando resultado processado para [{}] com [{}] tentativas",
                receiveMessage.id(), receiveMessage.equeueCount());
        var r = enqueue(result, responseQueueUrl, 0 );
        log.info("Enviado resultado processado [{}]", r);
        return result;
    }

    private void sentToFlow(String message) {
        log.info("Enviando resposta para o workflow: [{}]", message);
        var result = sentRestApi(message, workflowUri);
        log.info("Resposta do workflow: [{}]", result);
    }

    private String sentRestApi(Object body, String url) {
        log.info("Enviando para a API [{}], o objeto [{}]", url, body);
        return restTemplate.postForObject(url, body, String.class);
    }

    private void sendToRetry(ReceiveMessage msg) {
        // Incrementa o número de retentativas
        ReceiveMessage newMsg =  msg.enqueueIncrement();
        log.info("Reenfileirando [{}] para tentar novamente em [{}] segundos pela [{}] vez", msg.id(), delay, msg.equeueCount());
        var result = enqueue(newMsg, retryQueueUrl, delay);
        log.info("Enviado para a retentaiva[{}]", result);
    }

    private <T,R> R enqueue(T msg, String queueName, Integer delay) {
        var result = sqsTemplate.send(to -> to.queue(queueName)
                .payload(msg)
                .headers(Map.of("H1Delay", "VH1DelayValue", "H2Delay", "VH2DelayValue"))
                .delaySeconds(delay)
        );
        log.info("Mensagem [{}] publicada em [{}] com [{}] delay", msg, queueName, delay);
        return (R) result;
    }

}
