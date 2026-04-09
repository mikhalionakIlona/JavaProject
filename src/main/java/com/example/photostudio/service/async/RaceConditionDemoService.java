package com.example.photostudio.service.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceConditionDemoService {

    private final CounterService counterService;

    public String demonstrateRaceCondition() throws InterruptedException {
        return executeTest(false);
    }

    public String demonstrateSolution() throws InterruptedException {
        return executeTest(true);
    }

    private String executeTest(boolean useSynchronized) throws InterruptedException {
        counterService.resetCounters();

        int threadCount = 100;
        int incrementsPerThread = 1000;
        int expectedTotal = threadCount * incrementsPerThread;

        String testType = useSynchronized ? "РЕШЕНИЕ ЧЕРЕЗ SYNCHRONIZED" : "ДЕМОНСТРАЦИЯ RACE CONDITION";
        log.info("=== {} ===", testType);
        log.info("Потоков: {}, инкрементов на поток: {}, ожидаемое значение: {}",
                threadCount, incrementsPerThread, expectedTotal);

        long startTime = System.currentTimeMillis();
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        if (useSynchronized) {
                            counterService.incrementSync();
                        } else {
                            counterService.incrementUnsafe();
                        }
                    }
                });
            }

            executor.shutdown();

            boolean finished = executor.awaitTermination(1, TimeUnit.MINUTES);

            long endTime = System.currentTimeMillis();

            if (!finished) {
                executor.shutdownNow();
                return "ОШИБКА: Таймаут выполнения";
            }

            long result = useSynchronized ? counterService.getSyncCounter() : counterService.getUnsafeCounter();
            long lost = expectedTotal - result;

            String prefix = useSynchronized ? "SYNCHRONIZED РЕШЕНИЕ" : "RACE CONDITION ПРОБЛЕМА";

            log.info("{} результат: {} (ожидалось: {})", prefix, result, expectedTotal);
            log.info("Потеряно инкрементов: {}", lost);
            log.info("Время выполнения: {} ms", endTime - startTime);

            if (useSynchronized) {
                return String.format("Synchronized решение: результат=%d (ожидалось=%d, потеряно=%d, время=%d ms)",
                        result, expectedTotal, lost, endTime - startTime);
            } else {
                return String.format("Race condition проблема: результат=%d (ожидалось=%d, потеряно=%d, время=%d ms)",
                        result, expectedTotal, lost, endTime - startTime);
            }
        }
    }
}