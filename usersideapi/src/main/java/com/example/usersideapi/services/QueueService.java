package com.example.usersideapi.services;

import com.hazelcast.collection.IQueue;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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

    public QueueService(ExternalOutbound externalServiceAdapter, Bucket bucket, IQueue<String> queue,
                        ScheduledExecutorService executorService) {
        this.externalServiceAdapter = externalServiceAdapter;
        this.bucket = bucket;
        this.queue = queue;
        this.executorService = executorService;
    }

    @PostConstruct
    private void initListener() {
        executorService.execute(this::listen);
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
                externalServiceAdapter.doSomething(item);
                executorService.execute(this::listen);
                System.out.println("Consumed: " + item);
            } else {
                queue.put(item);
                executorService.schedule(this::listen, probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
                System.out.println("Resend: " + item);
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + item);
            executorService.execute(this::listen);
        }
    }
}
