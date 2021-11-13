package com.example.usersideapi.controllers;

import com.example.usersideapi.services.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final QueueService queueService;

    public MainController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/reverse")
    public ResponseEntity<String> reverse(@RequestParam("message") String message) {
        queueService.send(message);
        return ResponseEntity.ok("Message: " + message + "has been sent");
    }
}
