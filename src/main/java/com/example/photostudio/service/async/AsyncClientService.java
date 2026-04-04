package com.example.photostudio.service.async;

import com.example.photostudio.dto.async.AsyncTaskResponse;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncClientService {

    private final ClientService clientService;
    private final ApplicationContext applicationContext;
    private final Map<String, AsyncTaskResponse> taskStore = new ConcurrentHashMap<>();

    public String createClientAsync(ClientCreateDto request) {
        String taskId = UUID.randomUUID().toString();

        AsyncTaskResponse response = AsyncTaskResponse.builder()
                .taskId(taskId)
                .status("IN_PROGRESS")
                .createdAt(LocalDateTime.now())
                .message("Создание клиента начато...")
                .build();
        taskStore.put(taskId, response);

        AsyncClientService proxy = applicationContext.getBean(AsyncClientService.class);
        proxy.executeAsync(taskId, request);

        return taskId;
    }

    @Async("taskExecutor")
    @Transactional
    public void executeAsync(String taskId, ClientCreateDto request) {
        log.info("Асинхронная задача {} начата для клиента: {} {}",
                taskId, request.getFirstName(), request.getLastName());

        try {
            ClientDto result = clientService.createClient(request);

            AsyncTaskResponse response = taskStore.get(taskId);
            response.setStatus("COMPLETED");
            response.setCompletedAt(LocalDateTime.now());
            response.setResult(result);
            response.setClientId(result.getId());
            response.setClientName(result.getFirstName() + " " + result.getLastName());
            response.setMessage("Клиент успешно создан с ID: " + result.getId());

            log.info("Асинхронная задача {} завершена успешно. ID созданного клиента: {}", taskId, result.getId());

        } catch (Exception e) {
            log.error("Ошибка в асинхронной задаче {}: {}", taskId, e.getMessage());
            AsyncTaskResponse response = taskStore.get(taskId);
            response.setStatus("FAILED");
            response.setCompletedAt(LocalDateTime.now());
            response.setError(e.getMessage());
            response.setMessage("Ошибка при создании клиента: " + e.getMessage());
        }
    }

    public AsyncTaskResponse getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    public Map<String, AsyncTaskResponse> getAllTasks() {
        return taskStore;
    }
}