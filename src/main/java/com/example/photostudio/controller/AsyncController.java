package com.example.photostudio.controller;

import com.example.photostudio.dto.async.AsyncTaskDto;
import com.example.photostudio.service.async.AsyncClientService;
import com.example.photostudio.service.async.CounterService;
import com.example.photostudio.service.async.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
@Tag(name = "Асинхронные операции", description = "Управление асинхронными задачами")
public class AsyncController {

    private static final String TASK_ID_KEY = "taskId";
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";
    private static final String VALUE_KEY = "value";
    private static final String STATUS_COUNTERS_RESET = "counters reset";

    private final AsyncClientService asyncClientService;
    private final CounterService counterService;
    private final RaceConditionDemoService raceConditionDemoService;

    @PostMapping("/client")
    @Operation(summary = "Асинхронное создание клиента")
    public ResponseEntity<Map<String, String>> createClientAsync(
            @RequestParam @NotBlank(message = "Имя обязательно") String firstName,
            @RequestParam @NotBlank(message = "Фамилия обязательна") String lastName,
            @RequestParam @Pattern(regexp = "^\\+375\\d{9}$", message = "Неверный формат телефона") String phone,
            @RequestParam @Email(message = "Неверный формат email")
            @NotBlank(message = "Email обязателен") String email)
            throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = asyncClientService.createClientAsync(
                firstName, lastName, phone, email);
        String taskId = future.get();

        return ResponseEntity.ok(Map.of(
                TASK_ID_KEY, taskId,
                STATUS_KEY, "created",
                MESSAGE_KEY, "Задача создана. Используйте GET /api/async/tasks/{taskId} "
                        + "для проверки статуса"
        ));
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Получить статус асинхронной задачи")
    public ResponseEntity<AsyncTaskDto> getTaskStatus(@PathVariable String taskId) {
        AsyncTaskDto task = asyncClientService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Получить все асинхронные задачи")
    public ResponseEntity<Map<String, AsyncTaskDto>> getAllTasks() {
        return ResponseEntity.ok(asyncClientService.getAllTasks());
    }

    @GetMapping("/counter/atomic")
    @Operation(summary = "Получить значение Atomic счётчика")
    public ResponseEntity<Map<String, Integer>> getAtomicCounter() {
        return ResponseEntity.ok(Map.of("atomicCounter", counterService.getAtomicCounter()));
    }

    @GetMapping("/counter/sync")
    @Operation(summary = "Получить значение Synchronized счётчика")
    public ResponseEntity<Map<String, Integer>> getSyncCounter() {
        return ResponseEntity.ok(Map.of("syncCounter", counterService.getSyncCounter()));
    }

    @GetMapping("/counter/unsafe")
    @Operation(summary = "Получить значение небезопасного счётчика")
    public ResponseEntity<Map<String, Integer>> getUnsafeCounter() {
        return ResponseEntity.ok(Map.of("unsafeCounter", counterService.getUnsafeCounter()));
    }

    @PostMapping("/counter/increment-atomic")
    @Operation(summary = "Увеличить Atomic счётчик")
    public ResponseEntity<Map<String, Integer>> incrementAtomic() {
        return ResponseEntity.ok(Map.of(VALUE_KEY, counterService.incrementAtomic()));
    }

    @PostMapping("/counter/increment-sync")
    @Operation(summary = "Увеличить Synchronized счётчик")
    public ResponseEntity<Map<String, Integer>> incrementSync() {
        return ResponseEntity.ok(Map.of(VALUE_KEY, counterService.incrementSync()));
    }

    @PostMapping("/counter/increment-unsafe")
    @Operation(summary = "Увеличить небезопасный счётчик")
    public ResponseEntity<Map<String, Integer>> incrementUnsafe() {
        return ResponseEntity.ok(Map.of(VALUE_KEY, counterService.incrementUnsafe()));
    }

    @PostMapping("/counter/reset")
    @Operation(summary = "Сбросить все счётчики")
    public ResponseEntity<Map<String, String>> resetCounters() {
        counterService.resetCounters();
        return ResponseEntity.ok(Map.of(STATUS_KEY, STATUS_COUNTERS_RESET));
    }

    @PostMapping("/race-condition/demo")
    @Operation(summary = "Демонстрация race condition (100 потоков, 1000 итераций)")
    public ResponseEntity<String> demonstrateRaceCondition() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateRaceCondition();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/race-condition/synchronized")
    @Operation(summary = "Демонстрация решения race condition через synchronized")
    public ResponseEntity<String> demonstrateSynchronized() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateSynchronizedSolution();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/race-condition/atomic")
    @Operation(summary = "Демонстрация решения race condition через Atomic")
    public ResponseEntity<String> demonstrateAtomic() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateAtomicSolution();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/race-condition/compare")
    @Operation(summary = "Сравнение всех трёх подходов")
    public ResponseEntity<String> compareAll() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateAllApproaches();
        return ResponseEntity.ok(result);
    }
}