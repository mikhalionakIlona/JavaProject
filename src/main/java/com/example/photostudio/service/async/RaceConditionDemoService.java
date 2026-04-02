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

    private static final String LOST_INCREMENT_MESSAGE = "Потеряно инкрементов: {}";
    private static final String TIME_MESSAGE = "Время выполнения: {} ms";
    private static final String TIMEOUT_WARNING = "Таймаут ожидания завершения потоков";
    private static final String UNSAFE_COUNTER_LOG = "НЕПОТОКОБЕЗОПАСНЫЙ счетчик: {} (ожидалось: {})";
    private static final String SYNC_COUNTER_LOG = "SYNCHRONIZED счетчик: {} (ожидалось: {})";
    private static final String ATOMIC_COUNTER_LOG = "ATOMIC счетчик: {} (ожидалось: {})";
    private static final String RACE_CONDITION_START = "=== ДЕМОНСТРАЦИЯ RACE CONDITION ===";
    private static final String SYNC_SOLUTION_START = "=== РЕШЕНИЕ RACE CONDITION ЧЕРЕЗ SYNCHRONIZED ===";
    private static final String ATOMIC_SOLUTION_START = "=== РЕШЕНИЕ RACE CONDITION ЧЕРЕЗ ATOMIC ===";
    private static final String THREAD_START_LOG = "Запуск {} потоков, каждый делает {} инкрементов";
    private static final String EXPECTED_VALUE_LOG = "Ожидаемое значение: {}";
    private static final int THREAD_COUNT = 100;
    private static final int INCREMENTS_PER_THREAD = 1000;

    private final CounterService counterService;
    private final IncrementOperation unsafeOperation = new UnsafeOperation();
    private final IncrementOperation syncOperation = new SyncOperation();
    private final IncrementOperation atomicOperation = new AtomicOperation();

    private int runIncrementTest(String startLog, String resultLog, IncrementOperation operation)
            throws InterruptedException {
        log.info(startLog);
        counterService.resetCounters();

        int expectedTotal = THREAD_COUNT * INCREMENTS_PER_THREAD;

        log.info(THREAD_START_LOG, THREAD_COUNT, INCREMENTS_PER_THREAD);
        log.info(EXPECTED_VALUE_LOG, expectedTotal);

        long startTime = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        operation.increment();
                    }
                });
            }
            executor.shutdown();
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                log.warn(TIMEOUT_WARNING);
            }
        }

        long endTime = System.currentTimeMillis();
        int result = operation.getResult();
        int lost = expectedTotal - result;

        log.info(resultLog, result, expectedTotal);
        log.info(LOST_INCREMENT_MESSAGE, lost);
        log.info(TIME_MESSAGE, endTime - startTime);

        return result;
    }

    private interface IncrementOperation {
        void increment();
        int getResult();
    }

    private class UnsafeOperation implements IncrementOperation {
        @Override
        public void increment() {
            counterService.incrementUnsafe();
        }
        @Override
        public int getResult() {
            return counterService.getUnsafeCounter();
        }
    }

    private class SyncOperation implements IncrementOperation {
        @Override
        public void increment() {
            counterService.incrementSync();
        }
        @Override
        public int getResult() {
            return counterService.getSyncCounter();
        }
    }

    private class AtomicOperation implements IncrementOperation {
        @Override
        public void increment() {
            counterService.incrementAtomic();
        }
        @Override
        public int getResult() {
            return counterService.getAtomicCounter();
        }
    }

    public String demonstrateRaceCondition() throws InterruptedException {
        int result = runIncrementTest(
                RACE_CONDITION_START, UNSAFE_COUNTER_LOG, unsafeOperation);
        int expectedTotal = THREAD_COUNT * INCREMENTS_PER_THREAD;
        return String.format("Race condition результат: %d (потеряно %d инкрементов)",
                result, expectedTotal - result);
    }

    public String demonstrateSynchronizedSolution() throws InterruptedException {
        int result = runIncrementTest(
                SYNC_SOLUTION_START, SYNC_COUNTER_LOG, syncOperation);
        int expectedTotal = THREAD_COUNT * INCREMENTS_PER_THREAD;
        return String.format("Synchronized результат: %d (потеряно %d)",
                result, expectedTotal - result);
    }

    public String demonstrateAtomicSolution() throws InterruptedException {
        int result = runIncrementTest(
                ATOMIC_SOLUTION_START, ATOMIC_COUNTER_LOG, atomicOperation);
        int expectedTotal = THREAD_COUNT * INCREMENTS_PER_THREAD;
        return String.format("Atomic результат: %d (потеряно %d)",
                result, expectedTotal - result);
    }

    public String demonstrateAllApproaches() throws InterruptedException {
        return "=== СРАВНЕНИЕ ПОДХОДОВ К ПОТОКОБЕЗОПАСНОСТИ ===\n\n"
                + "1. " + demonstrateRaceCondition() + "\n\n"
                + "2. " + demonstrateSynchronizedSolution() + "\n\n"
                + "3. " + demonstrateAtomicSolution() + "\n\n"
                + "=== ВЫВОД ===\n"
                + "Race condition: значения теряются из-за отсутствия синхронизации\n"
                + "Synchronized: медленнее, но гарантирует корректность\n"
                + "Atomic: быстрее synchronized, также гарантирует корректность\n";
    }
}