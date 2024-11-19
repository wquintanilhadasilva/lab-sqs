package com.lab.sqs.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@Slf4j
public class ApiService {

    private static Boolean hasReject = Boolean.FALSE;

    public ReceiverResult send(MessageInput message) {
        if (hasReject) {
            log.error("Configurado para rejeitar a mensagem [{}]", message);
            throw new RuntimeException("Mensagem Rejeitada");
        }
        log.info("Processando a mensagem: [{}]", message);
        return new ReceiverResult(ZonedDateTime.now(), String.format("Mensagem %s processada", message.id()));
    }

    public void setHasReject(Boolean reject) {
        hasReject = reject;
    }

}
