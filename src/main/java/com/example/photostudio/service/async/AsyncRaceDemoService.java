package com.example.photostudio.service.async;

import com.example.photostudio.dto.AsyncTaskDto;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncRaceDemoService {

    private final ClientService clientService;
    private final Map<String, AsyncTaskDto> tasks = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> createClientsAsync(List<ClientCreateDto> clients) {
        String taskId = UUID.randomUUID().toString();
        log.info("Асинхронная задача создана: {}", taskId);

        AsyncTaskDto task = AsyncTaskDto.builder()
                .taskId(taskId)
                .status("IN_PROGRESS")
                .message("Обработка начата...")
                .startTime(LocalDateTime.now())
                .totalCount(clients.size())
                .processedCount(0)
                .build();
        tasks.put(taskId, task);

        try {
            Thread.sleep(2000);
            List<ClientDto> results = clientService.createClientsBulk(clients);
            task.setStatus("COMPLETED");
            task.setMessage("Успешно создано " + results.size() + " клиентов");
            task.setProcessedCount(results.size());
            task.setEndTime(LocalDateTime.now());
            log.info("Задача {} завершена успешно", taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus("FAILED");
            task.setMessage("Операция прервана");
            task.setEndTime(LocalDateTime.now());
            log.error("Задача {} была прервана", taskId);
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setMessage("Ошибка: " + e.getMessage());
            task.setEndTime(LocalDateTime.now());
            log.error("Ошибка задачи {}: {}", taskId, e.getMessage());
        }

        return CompletableFuture.completedFuture(taskId);
    }

    public AsyncTaskDto getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    private final AtomicInteger safeCounter = new AtomicInteger(0);
    private int unsafeCounter = 0;

    public int incrementSafe() {
        return safeCounter.incrementAndGet();
    }

    public int getSafeCounter() {
        return safeCounter.get();
    }

    public void resetCounters() {
        safeCounter.set(0);
        unsafeCounter = 0;
        log.info("Счётчики сброшены");
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Executor не завершился");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private int runUnsafeOperation(int threadCount, int iterationsPerThread) throws InterruptedException {
        unsafeCounter = 0;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        unsafeCounter++;
                    }
                    latch.countDown();
                });
            }

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Небезопасная операция не завершилась за 30 секунд");
            }
        } finally {
            shutdownExecutor(executor);
        }
        return unsafeCounter;
    }

    private int runSafeAtomicOperation(int threadCount, int iterationsPerThread) throws InterruptedException {
        safeCounter.set(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        safeCounter.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Безопасная операция не завершилась за 30 секунд");
            }
        } finally {
            shutdownExecutor(executor);
        }
        return safeCounter.get();
    }

    public RaceDemoResult demonstrateRaceCondition(int threadCount, int iterationsPerThread) {
        log.info("Race condition demo: {} потоков, {} итераций", threadCount, iterationsPerThread);

        int expected = threadCount * iterationsPerThread;
        long unsafeTime;
        int unsafeResult;

        try {
            long startTime = System.currentTimeMillis();
            unsafeResult = runUnsafeOperation(threadCount, iterationsPerThread);
            unsafeTime = System.currentTimeMillis() - startTime;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Небезопасная операция прервана", e);
            unsafeResult = 0;
            unsafeTime = 0;
        }

        long safeTime;
        int safeResult;

        try {
            long startTime = System.currentTimeMillis();
            safeResult = runSafeAtomicOperation(threadCount, iterationsPerThread);
            safeTime = System.currentTimeMillis() - startTime;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Безопасная операция прервана", e);
            safeResult = 0;
            safeTime = 0;
        }

        log.info("Результат: ожидалось={}, unsafe={}, safe={}, потеряно={}",
                expected, unsafeResult, safeResult, expected - unsafeResult);

        return RaceDemoResult.builder()
                .threadCount(threadCount)
                .iterationsPerThread(iterationsPerThread)
                .expectedCount(expected)
                .unsafeCount(unsafeResult)
                .safeCount(safeResult)
                .lostUpdates(expected - unsafeResult)
                .unsafeTimeMs(unsafeTime)
                .safeTimeMs(safeTime)
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class RaceDemoResult {
        private int threadCount;
        private int iterationsPerThread;
        private int expectedCount;
        private int unsafeCount;
        private int safeCount;
        private int lostUpdates;
        private long unsafeTimeMs;
        private long safeTimeMs;
    }
}