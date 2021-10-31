package com.example.externalservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageProcessorController {

    @GetMapping("/doSomething")
    public ResponseEntity<String> doSomething(String message) {
        StringBuilder sb = new StringBuilder(message);
        sb.reverse();
        return ResponseEntity.ok(sb.toString());
    }
}
