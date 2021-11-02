package com.example.externalservice.controllers;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class MessageProcessorController {
    private final Bucket bucket;

    public MessageProcessorController() {
        this.bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(10, Duration.ofSeconds(10))))
                .build();
    }

    @GetMapping("/doSomething")
    public ResponseEntity<String> doSomething(@RequestParam("message") String message) {
        if (bucket.tryConsume(1)) {
            StringBuilder sb = new StringBuilder(message);
            sb.reverse();
            return ResponseEntity.ok(sb.toString());
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
