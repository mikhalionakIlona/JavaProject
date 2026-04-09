package com.example.photostudio.service.async;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Getter
@Service
public class CounterService {

    private long unsafeCounter = 0;

    private final AtomicLong atomicCounter = new AtomicLong(0);

    private long syncCounter = 0;

    public void incrementUnsafe() {
        unsafeCounter++;
    }

    public synchronized void incrementSync() {
        syncCounter++;
    }


    public void resetCounters() {
        unsafeCounter = 0;
        syncCounter = 0;
        atomicCounter.set(0);
    }
}