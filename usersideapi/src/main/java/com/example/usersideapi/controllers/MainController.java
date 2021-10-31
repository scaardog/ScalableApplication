package com.example.usersideapi.controllers;

import com.example.usersideapi.services.ExternalServiceAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final ExternalServiceAdapter externalServiceAdapter;

    public MainController(ExternalServiceAdapter externalServiceAdapter) {
        this.externalServiceAdapter = externalServiceAdapter;
    }

    @GetMapping("/reverse")
    public ResponseEntity<String> reverse(@RequestParam("message") String message) {
        return ResponseEntity.ok(externalServiceAdapter.doSomething(message));
    }
}
