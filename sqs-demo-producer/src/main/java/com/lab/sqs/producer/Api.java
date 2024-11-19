package com.lab.sqs.producer;

import lombok.RequiredArgsConstructor;
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

    @PostMapping("/send")
    public ResponseEntity<SendResult> send(@RequestBody String message) {
        return ResponseEntity.ok(apiService.send(message));
    }

}
