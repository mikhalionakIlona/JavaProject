package com.example.photostudio.controller;

import com.example.photostudio.dto.photograher.PhotographerDto;
import com.example.photostudio.dto.photograher.PhotographerCreateDto;
import com.example.photostudio.dto.photograher.PhotographerUpdateDto;
import com.example.photostudio.dto.ErrorResponse;
import com.example.photostudio.service.PhotographerService;
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
@RequestMapping("/api/photographers")
@RequiredArgsConstructor
@Tag(name = "Фотографы", description = "Управление фотографами")
public class PhotographerController {
    private final PhotographerService service;

    @GetMapping
    @Operation(summary = "Получить всех фотографов")
    public ResponseEntity<List<PhotographerDto>> getAllPhotographers() {
        return ResponseEntity.ok(service.getAllPhotographers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фотографа по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотограф найден"),
        @ApiResponse(responseCode = "404", description = "Фотограф не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotographerDto> getPhotographerById(
            @Parameter(description = "ID фотографа", required = true) @PathVariable Long id) {
        PhotographerDto photographer = service.getPhotographerById(id);
        return photographer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(photographer);
    }

    @PostMapping
    @Operation(summary = "Создать нового фотографа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотограф создан"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotographerDto> createPhotographer(@Valid @RequestBody PhotographerCreateDto createDto) {
        return ResponseEntity.ok(service.createPhotographer(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить фотографа")
    public ResponseEntity<PhotographerDto> updatePhotographer(@PathVariable Long id,
                                                              @Valid @RequestBody PhotographerUpdateDto updateDto) {
        PhotographerDto updated = service.updatePhotographer(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фотографа")
    public ResponseEntity<Void> deletePhotographer(@PathVariable Long id) {
        return service.deletePhotographer(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}