package com.example.usersideapi.services;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalServiceAdapter implements ExternalOutbound {
    private final String remoteUrl;
    private final RestTemplate restTemplate;
    private final HazelcastInstance hazelcastClient;


    public ExternalServiceAdapter(@Value("${externalservice.url}") String remoteUrl, RestTemplateBuilder restTemplateBuilder,
                                  HazelcastInstance hazelcastClient) {
        this.remoteUrl = remoteUrl;
        this.restTemplate = restTemplateBuilder.build();
        this.hazelcastClient = hazelcastClient;
    }

    @Override
    public String doSomething(String message) {
        Integer value = (Integer) hazelcastClient.getCPSubsystem().getAtomicReference("bucket").get();
        return value.toString();
        /*String endpoint = remoteUrl + "/doSomething?message=" + message;
        return this.restTemplate.getForObject(endpoint, String.class);*/
    }
}
