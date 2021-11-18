package com.example.usersideapi.services;

import com.hazelcast.collection.IQueue;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class QueueService {
    private final ExternalOutbound externalServiceAdapter;
    private final Bucket bucket;
    private final IQueue<String> queue;
    private final ScheduledExecutorService executorService;
    private final int poolSize;

    public QueueService(ExternalOutbound externalServiceAdapter, Bucket bucket, IQueue<String> queue,
                        ScheduledExecutorService executorService, @Value("${executorservice.poolsize}") int poolSize) {
        this.externalServiceAdapter = externalServiceAdapter;
        this.bucket = bucket;
        this.queue = queue;
        this.executorService = executorService;
        this.poolSize = poolSize;
    }

    @PostConstruct
    private void initListener() {
        for (int i = 0; i < poolSize; i++) {
            executorService.execute(this::listen);
        }
    }

    public void send(String item) {
        try {
            queue.put(item);
            System.out.println("Item sent: " + item);
        } catch (Exception e) {
            System.out.println("Error occurred, item wasn't sent : " + item);
        }
    }

    void listen() {
        String item = null;
        try {
            item = queue.take();
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                try {
                    externalServiceAdapter.doSomething(item);
                    System.out.println("Consumed: " + item);
                } catch (Exception e) {
                    queue.put(item);
                }
                executorService.execute(this::listen);
            } else {
                System.out.println("Resend: " + item);
                queue.put(item);
                executorService.schedule(this::listen, probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + item);
            executorService.execute(this::listen);
        }
    }
}
