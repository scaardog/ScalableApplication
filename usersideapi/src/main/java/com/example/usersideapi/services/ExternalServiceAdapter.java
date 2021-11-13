package com.example.usersideapi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalServiceAdapter implements ExternalOutbound {
    private final RestTemplate restTemplate;

    public ExternalServiceAdapter(RestTemplate externalServiceRestTemplate) {
        this.restTemplate = externalServiceRestTemplate;
    }

    @Override
    public String doSomething(String message) {
        String endpoint = "/do-something?message={message}";
        return restTemplate.getForObject(endpoint, String.class, message);
    }
}
