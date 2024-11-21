package com.lab.sqs.receiver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class Api {

    private final ApiService apiService;

    @PostMapping("/process")
    public ResponseEntity<ReceiverResult> send(@RequestBody MessageInput message) {
        try {
            return ResponseEntity.ok(apiService.send(message));
        } catch (Exception ex) {
            return new ResponseEntity<ReceiverResult>(new ReceiverResult(ZonedDateTime.now(), ex.getMessage()), HttpStatus.BAD_REQUEST) ;
        }
    }

    @PostMapping("/reject-enabled")
    public ResponseEntity<String> rejectEnabled() {
        apiService.setHasReject(Boolean.TRUE);
        return new ResponseEntity<String>("Rejeição de documentos habilitada!", HttpStatus.ACCEPTED);
    }

    @PostMapping("/reject-disabled")
    public ResponseEntity<String> rejectDisabled() {
        apiService.setHasReject(Boolean.FALSE);
        return new ResponseEntity<String>("Rejeição de documentos desabilitada!", HttpStatus.ACCEPTED);
    }

}
