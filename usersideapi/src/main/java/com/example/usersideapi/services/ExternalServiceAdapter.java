package com.example.usersideapi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalServiceAdapter implements ExternalOutbound {
    private final String remoteUrl;
    private final RestTemplate restTemplate;

    public ExternalServiceAdapter(@Value("${externalservice.url}") String remoteUrl, RestTemplateBuilder restTemplateBuilder) {
        this.remoteUrl = remoteUrl;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String doSomething(String message) {
        String endpoint = remoteUrl + "/doSomething?message=" + message;
        return this.restTemplate.getForObject(endpoint, String.class);
    }
}
