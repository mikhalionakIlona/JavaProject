package com.example.photostudio.controller;

import com.example.photostudio.dto.async.AsyncTaskResponse;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.service.async.AsyncClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/async/client")
@RequiredArgsConstructor
@Tag(name = "Асинхронные операции", description = "Асинхронное создание клиентов")
public class AsyncController {

    private final AsyncClientService asyncClientService;

    @PostMapping
    @Operation(summary = "Асинхронное создание клиента")
    public ResponseEntity<AsyncTaskResponse> createClientAsync(@RequestBody ClientCreateDto clientDto) {
        String taskId = asyncClientService.createClientAsync(clientDto);

        AsyncTaskResponse response = AsyncTaskResponse.builder()
                .taskId(taskId)
                .status("ACCEPTED")
                .createdAt(LocalDateTime.now())
                .message("Задача принята в обработку")
                .build();

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Получить статус асинхронной задачи")
    public ResponseEntity<AsyncTaskResponse> getTaskStatus(@PathVariable String taskId) {
        AsyncTaskResponse response = asyncClientService.getTaskStatus(taskId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Получить все асинхронные задачи")
    public ResponseEntity<Map<String, AsyncTaskResponse>> getAllTasks() {
        return ResponseEntity.ok(asyncClientService.getAllTasks());
    }
}