package com.example.usersideapi.services;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class ExternalServiceAdapter implements ExternalOutbound {
    private final String remoteUrl;
    private final RestTemplate restTemplate;
    private final HazelcastInstance hazelcastClient;
    private final Bucket bucket;
    private static final String bucketId = "globalBucket";

    public ExternalServiceAdapter(@Value("${externalservice.url}") String remoteUrl, RestTemplateBuilder restTemplateBuilder,
                                  HazelcastInstance hazelcastClient) {
        this.remoteUrl = remoteUrl;
        this.restTemplate = restTemplateBuilder.build();
        this.hazelcastClient = hazelcastClient;
        IMap<String, GridBucketState> cache = hazelcastClient.getMap("bucket");
        bucket = Bucket4j.extension(io.github.bucket4j.grid.hazelcast.Hazelcast.class).builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(10, Duration.ofSeconds(10))))
                .build(cache, bucketId, RecoveryStrategy.RECONSTRUCT);
    }

    @Override
    public String doSomething(String message) {
        if (bucket.tryConsume(1)) {
            String endpoint = remoteUrl + "/doSomething?message=" + message;
            return this.restTemplate.getForObject(endpoint, String.class);
        } else {
            return "NoMoreTickets";
        }
    }
}
