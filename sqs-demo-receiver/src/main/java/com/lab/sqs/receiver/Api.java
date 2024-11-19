package com.lab.sqs.receiver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class Api {

    private final ApiService apiService;

    @PostMapping("/process")
    public ResponseEntity<ReceiverResult> send(@RequestBody MessageInput message) {
        return ResponseEntity.ok(apiService.send(message));
    }

    @PostMapping("/reject-enabled")
    public ResponseEntity<String> rejectEnabled() {
        apiService.setHasReject(Boolean.TRUE);
        return new ResponseEntity<String>("Rejeição de documentos habilitada!", HttpStatus.ACCEPTED);
    }

    @PostMapping("/reject-disabled")
    public ResponseEntity<String> rejectDisabled() {
        apiService.setHasReject(Boolean.TRUE);
        return new ResponseEntity<String>("Rejeição de documentos desabilitada!", HttpStatus.ACCEPTED);
    }

}
