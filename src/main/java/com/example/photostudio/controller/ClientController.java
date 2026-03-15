package com.example.photostudio.controller;

import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientUpdateDto;
import com.example.photostudio.dto.ErrorResponse;
import com.example.photostudio.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Клиенты", description = "Управление клиентами фотостудии")
public class ClientController {
    private final ClientService service;

    @GetMapping
    @Operation(summary = "Получить всех клиентов", description = "Возвращает список всех клиентов")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        return ResponseEntity.ok(service.getAllClients());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить клиента по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Клиент найден"),
        @ApiResponse(responseCode = "404", description = "Клиент не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientDto> getClientById(
            @Parameter(description = "ID клиента", required = true) @PathVariable Long id) {
        ClientDto client = service.getClientById(id);
        return client == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(client);
    }

    @PostMapping
    @Operation(summary = "Создать нового клиента")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Клиент создан"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientCreateDto createDto) {
        return ResponseEntity.ok(service.createClient(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить клиента")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id,
                                                  @Valid @RequestBody ClientUpdateDto updateDto) {
        ClientDto updated = service.updateClient(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить клиента")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        return service.deleteClient(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}