package com.example.usersideapi.services;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class QueueService {
    private final ExternalServiceAdapter externalServiceAdapter;
    private final Bucket bucket;
    private final IQueue<String> queue;
    private final ScheduledExecutorService service;

    public QueueService(HazelcastInstance hazelcastInstance, ExternalServiceAdapter externalServiceAdapter,
                        Bucket bucket) {
        this.externalServiceAdapter = externalServiceAdapter;
        this.bucket = bucket;
        this.queue = hazelcastInstance.getQueue("queue");
        this.service = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    private void initListener() {
        service.execute(this::listen);
    }

    public void send(String item) {
        try {
            queue.put(item);
        } catch (Exception e) {
            System.out.println("Send: " + item);
        }
    }

    private void listen() {
        String item = null;
        try {
            item = queue.take();
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                System.out.println("Consumed: " + item);
                externalServiceAdapter.doSomething(item);
                service.execute(this::listen);
            } else {
                System.out.println("Resend: " + item);
                queue.put(item);
                service.schedule(this::listen, probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException ie) {
            System.out.println("The listener has been interrupted");
        } catch (Exception e) {
            System.out.println("Error occurred: " + item + "Stacktrace: \n" + Arrays.toString(e.getStackTrace()));
            throw e;
        }
    }
}
