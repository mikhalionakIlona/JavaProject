package com.example.photostudio.controller;

import com.example.photostudio.dto.service.ServiceDto;
import com.example.photostudio.dto.service.ServiceCreateDto;
import com.example.photostudio.dto.service.ServiceUpdateDto;
import com.example.photostudio.dto.ErrorResponse;
import com.example.photostudio.service.PhotoServiceService;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Услуги", description = "Управление услугами")
public class ServiceController {
    private final PhotoServiceService service;

    @GetMapping
    @Operation(summary = "Получить все услуги")
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(service.getAllServices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить услугу по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Услуга найдена"),
        @ApiResponse(responseCode = "404", description = "Услуга не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceDto> getServiceById(
            @Parameter(description = "ID услуги", required = true) @PathVariable Long id) {
        ServiceDto serviceDto = service.getServiceById(id);
        return serviceDto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(serviceDto);
    }

    @PostMapping
    @Operation(summary = "Создать новую услугу")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Услуга создана"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreateDto createDto) {
        return ResponseEntity.ok(service.createService(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить услугу")
    public ResponseEntity<ServiceDto> updateService(@PathVariable Long id,
                                                    @Valid @RequestBody ServiceUpdateDto updateDto) {
        ServiceDto updated = service.updateService(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить услугу")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        return service.deleteService(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}