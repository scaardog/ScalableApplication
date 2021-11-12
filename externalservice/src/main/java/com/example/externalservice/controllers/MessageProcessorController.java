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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

@RestController
public class MessageProcessorController {
    private final Bucket bucket;
    private static final String fileName = "D:\\Repositories\\ScalableApplication\\externalservice\\src\\main\\resources\\DoSomething.txt";

    public MessageProcessorController() {
        this.bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofSeconds(1))))
                .build();
    }

    @GetMapping("/doSomething")
    public ResponseEntity<String> doSomething(@RequestParam("message") String message) {
        if (bucket.tryConsume(1)) {
            try {
                Files.writeString(
                        Path.of(fileName),
                        message + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return ResponseEntity.ok("Сообщение получено");
        }

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
