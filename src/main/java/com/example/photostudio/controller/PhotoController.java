package com.example.photostudio.controller;

import com.example.photostudio.dto.photo.PhotoDto;
import com.example.photostudio.dto.photo.PhotoCreateDto;
import com.example.photostudio.dto.ErrorResponse;
import com.example.photostudio.service.PhotoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Tag(name = "Фотографии", description = "Управление фотографиями")
public class PhotoController {
    private final PhotoService service;

    @GetMapping
    @Operation(summary = "Получить все фотографии")
    public ResponseEntity<List<PhotoDto>> getAllPhotos() {
        return ResponseEntity.ok(service.getAllPhotos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фотографию по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотография найдена"),
        @ApiResponse(responseCode = "404", description = "Фотография не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotoDto> getPhotoById(
            @Parameter(description = "ID фотографии", required = true) @PathVariable Long id) {
        PhotoDto photo = service.getPhotoById(id);
        return photo == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(photo);
    }

    @GetMapping("/by-session/{sessionId}")
    @Operation(summary = "Получить фотографии по ID сессии")
    public ResponseEntity<List<PhotoDto>> getPhotosBySessionId(
            @Parameter(description = "ID сессии", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(service.getPhotosBySessionId(sessionId));
    }

    @PostMapping
    @Operation(summary = "Создать новую фотографию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотография создана"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotoDto> createPhoto(@Valid @RequestBody PhotoCreateDto createDto) {
        return ResponseEntity.ok(service.createPhoto(
                createDto.getFileName(),
                createDto.getFilePath(),
                createDto.getSessionId()
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фотографию")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        return service.deletePhoto(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}