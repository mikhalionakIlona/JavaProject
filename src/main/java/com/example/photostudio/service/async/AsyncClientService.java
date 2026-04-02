package com.example.photostudio.service.async;

import com.example.photostudio.dto.async.AsyncTaskDto;
import com.example.photostudio.model.Client;
import com.example.photostudio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncClientService {

    private final ClientRepository clientRepository;

    private final Map<String, AsyncTaskDto> tasks = new ConcurrentHashMap<>();

    @Async
    @Transactional
    public CompletableFuture<String> createClientAsync(String firstName, String lastName, String phone, String email) {
        String taskId = UUID.randomUUID().toString();
        log.info("Асинхронная задача создана: {}", taskId);

        AsyncTaskDto task = AsyncTaskDto.builder()
                .taskId(taskId)
                .status("IN_PROGRESS")
                .message("Создание клиента начато...")
                .startTime(LocalDateTime.now())
                .clientName(firstName + " " + lastName)
                .build();
        tasks.put(taskId, task);

        try {
            Thread.sleep(3000);

            Client client = Client.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(phone)
                    .email(email)
                    .build();

            Client savedClient = clientRepository.save(client);

            task.setStatus("COMPLETED");
            task.setMessage("Клиент успешно создан с ID: " + savedClient.getId());
            task.setEndTime(LocalDateTime.now());
            task.setClientId(savedClient.getId());

            log.info("Асинхронная задача {} завершена, создан клиент ID: {}", taskId, savedClient.getId());
            return CompletableFuture.completedFuture(taskId);

        } catch (InterruptedException e) {
            log.error("Асинхронная задача {} прервана: {}", taskId, e.getMessage());
            task.setStatus("FAILED");
            task.setMessage("Задача прервана");
            task.setEndTime(LocalDateTime.now());
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(taskId);

        } catch (Exception e) {
            log.error("Ошибка при выполнении асинхронной задачи {}: {}", taskId, e.getMessage());
            task.setStatus("FAILED");
            task.setMessage("Ошибка: " + e.getMessage());
            task.setEndTime(LocalDateTime.now());
            return CompletableFuture.completedFuture(taskId);
        }
    }

    public AsyncTaskDto getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    public Map<String, AsyncTaskDto> getAllTasks() {
        return tasks;
    }
}