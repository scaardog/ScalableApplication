package com.example.usersideapi.services;

import com.hazelcast.collection.IQueue;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    ExternalServiceAdapter externalServiceAdapter = mock(ExternalServiceAdapter.class);
    Bucket bucket = mock(Bucket.class);
    IQueue<String> queue = mock(IQueue.class);
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);

    ConsumptionProbe probe = mock(ConsumptionProbe.class);

    QueueService queueService = new QueueService(externalServiceAdapter, bucket, queue, executorService);

    @Test
    void sendTest() throws InterruptedException {
        String message = "testMessage";

        doNothing().when(executorService).execute(any());

        queueService.send(message);
        verify(queue).put(message);
    }

    @Test
    void listenTokenIsConsumedTest() throws InterruptedException {
        String message = "testMessage";

        doNothing().when(executorService).execute(any());
        when(queue.take()).thenReturn(message);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);

        queueService.listen();
        verify(externalServiceAdapter).doSomething(message);
        verify(executorService).execute(any(Runnable.class));
    }

    @Test
    void listenTokenIsNotConsumedTest() throws InterruptedException {
        String message = "testMessage";
        long nanos = 11111L;

        doNothing().when(executorService).execute(any());
        when(queue.take()).thenReturn(message);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(nanos);

        queueService.listen();
        verify(queue).put(message);
        verify(externalServiceAdapter,times(0)).doSomething(message);
        verify(executorService).schedule(any(Runnable.class), eq(nanos), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void listenExceptionTest() throws InterruptedException{

        doNothing().when(executorService).execute(any());
        when(queue.take()).thenThrow(InterruptedException.class);

        queueService.listen();
        verify(executorService).execute(any(Runnable.class));
    }
}