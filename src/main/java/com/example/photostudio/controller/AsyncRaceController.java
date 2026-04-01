package com.example.photostudio.controller;

import com.example.photostudio.dto.AsyncTaskDto;
import com.example.photostudio.dto.client.ClientBulkCreateDto;
import com.example.photostudio.service.async.AsyncRaceDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Демонстрация", description = "Асинхронные операции и race condition")
public class AsyncRaceController {

    private final AsyncRaceDemoService demoService;

    @PostMapping("/async/bulk")
    @Operation(summary = "Асинхронное массовое создание клиентов")
    public ResponseEntity<Map<String, String>> createClientsAsync(
            @Valid @RequestBody ClientBulkCreateDto dto) throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = demoService.createClientsAsync(dto.getClients());
        String taskId = future.get();

        return ResponseEntity.ok(Map.of(
                "taskId", taskId,
                "message", "Используйте GET /api/demo/async/status/{taskId} для проверки статуса"
        ));
    }

    @GetMapping("/async/status/{taskId}")
    @Operation(summary = "Получить статус асинхронной задачи")
    public ResponseEntity<AsyncTaskDto> getTaskStatus(@PathVariable String taskId) {
        AsyncTaskDto task = demoService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/counter")
    @Operation(summary = "Получить значение счётчика")
    public ResponseEntity<Map<String, Integer>> getCounter() {
        return ResponseEntity.ok(Map.of("counter", demoService.getSafeCounter()));
    }

    @PostMapping("/counter/increment")
    @Operation(summary = "Увеличить счётчик")
    public ResponseEntity<Map<String, Integer>> incrementCounter() {
        return ResponseEntity.ok(Map.of("counter", demoService.incrementSafe()));
    }

    @PostMapping("/counter/reset")
    @Operation(summary = "Сбросить счётчик")
    public ResponseEntity<Map<String, String>> resetCounter() {
        demoService.resetCounters();
        return ResponseEntity.ok(Map.of("status", "reset"));
    }

    @PostMapping("/race")
    @Operation(summary = "Демонстрация race condition")
    public ResponseEntity<AsyncRaceDemoService.RaceDemoResult> demonstrateRaceCondition(
            @RequestParam(defaultValue = "50") int threadCount,
            @RequestParam(defaultValue = "1000") int iterationsPerThread) {

        return ResponseEntity.ok(demoService.demonstrateRaceCondition(threadCount, iterationsPerThread));
    }
}