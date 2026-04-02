package com.example.photostudio.service.async;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CounterService {

    private final AtomicInteger atomicCounter = new AtomicInteger(0);
    @Getter
    private int syncCounter = 0;
    @Getter
    private int unsafeCounter = 0;

    public int incrementAtomic() {
        return atomicCounter.incrementAndGet();
    }

    public synchronized int incrementSync() {
        syncCounter++;
        return syncCounter;
    }

    public int incrementUnsafe() {
        int current = unsafeCounter;
        unsafeCounter = current + 1;
        return unsafeCounter;
    }

    public int getAtomicCounter() {
        return atomicCounter.get();
    }

    public void resetCounters() {
        atomicCounter.set(0);
        syncCounter = 0;
        unsafeCounter = 0;
        log.info("Все счётчики сброшены");
    }
}