package com.example.usersideapi.services;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class QueueService {
    private final ExternalServiceAdapter externalServiceAdapter;
    private final Bucket bucket;
    private static final String bucketId = "globalBucket";
    private final IQueue<String> queue;
    public QueueService(HazelcastInstance hazelcastInstance, ExternalServiceAdapter externalServiceAdapter) {
        this.externalServiceAdapter = externalServiceAdapter;
        IMap<String, GridBucketState> cache = hazelcastInstance.getMap("bucket");
        bucket = Bucket4j.extension(io.github.bucket4j.grid.hazelcast.Hazelcast.class).builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(10, Duration.ofSeconds(10))))
                .build(cache, bucketId, RecoveryStrategy.RECONSTRUCT);
        queue = hazelcastInstance.getQueue("queue");
    }

    @PostConstruct
    private void initListener() {
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(this::listen);
    }

    public void send(String item) {
        try {
            queue.put(item);
        } catch (Exception e) {
            System.out.println("Send: " + item);
        }
    }

    private void listen() {
        while (true) {
            String item = null;
            try {
                item = queue.take();
                if (bucket.tryConsume(1)) {
                    System.out.println("Consumed: " + item);
                    externalServiceAdapter.doSomething(item);
                } else {
                    System.out.println("Resend: " + item);
                    queue.put(item);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                System.out.println("Error occured: " + item);
            }
        }
    }

}
