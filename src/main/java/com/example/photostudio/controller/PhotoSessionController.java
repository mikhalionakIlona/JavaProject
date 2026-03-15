package com.example.photostudio.controller;

import com.example.photostudio.dto.photosession.PhotoSessionDto;
import com.example.photostudio.dto.photosession.PhotoSessionCreateDto;
import com.example.photostudio.dto.photosession.PhotoSessionUpdateDto;
import com.example.photostudio.dto.photosession.PhotoSessionFilterDto;
import com.example.photostudio.dto.ErrorResponse;
import com.example.photostudio.service.PhotoSessionService;
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
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/photo-sessions")
@RequiredArgsConstructor
@Tag(name = "Фотосессии", description = "Управление фотосессиями")
public class PhotoSessionController {
    private final PhotoSessionService photoSessionService;

    @GetMapping
    @Operation(summary = "Получить все фотосессии")
    public ResponseEntity<List<PhotoSessionDto>> getAllPhotoSessions() {
        return ResponseEntity.ok(photoSessionService.getAllPhotoSessions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фотосессию по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотосессия найдена"),
        @ApiResponse(responseCode = "404", description = "Фотосессия не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotoSessionDto> getPhotoSessionById(
            @Parameter(description = "ID фотосессии", required = true) @PathVariable Long id) {
        PhotoSessionDto dto = photoSessionService.getPhotoSessionById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Получить фотосессии по ID клиента")
    public ResponseEntity<List<PhotoSessionDto>> getPhotoSessionsByClientId(
            @Parameter(description = "ID клиента", required = true) @PathVariable Long clientId) {
        return ResponseEntity.ok(photoSessionService.getPhotoSessionsByClientId(clientId));
    }

    @PostMapping
    @Operation(summary = "Создать новую фотосессию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фотосессия создана"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PhotoSessionDto> createPhotoSession(@Valid @RequestBody PhotoSessionCreateDto createDto) {
        return ResponseEntity.ok(photoSessionService.createPhotoSession(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить фотосессию")
    public ResponseEntity<PhotoSessionDto> updatePhotoSession(
            @PathVariable Long id, @Valid @RequestBody PhotoSessionUpdateDto updateDto) {
        PhotoSessionDto updated = photoSessionService.updatePhotoSession(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фотосессию")
    public ResponseEntity<Void> deletePhotoSession(@PathVariable Long id) {
        return photoSessionService.deletePhotoSession(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/demonstrate/nplus1")
    @Operation(summary = "Демонстрация N+1 проблемы")
    public ResponseEntity<String> demonstrateNPlus1() {
        photoSessionService.demonstrateNPlus1Problem();
        return ResponseEntity.ok("N+1 проблема продемонстрирована. Проверьте логи.");
    }

    @GetMapping("/demonstrate/entity-graph")
    @Operation(summary = "Демонстрация решения с EntityGraph")
    public ResponseEntity<String> demonstrateEntityGraph() {
        photoSessionService.demonstrateEntityGraphSolution();
        return ResponseEntity.ok("Решение с EntityGraph продемонстрировано. Проверьте логи.");
    }

    @PostMapping("/demonstrate/with-transaction")
    @Operation(summary = "Демонстрация создания с транзакцией")
    public ResponseEntity<String> createWithTransaction(@RequestBody PhotoSessionCreateDto createDto) {
        try {
            photoSessionService.createWithRelatedWithTransaction(createDto);
            return ResponseEntity.ok("Операция с транзакцией выполнена успешно");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " - транзакция откачена");
        }
    }

    @PostMapping("/demonstrate/without-transaction")
    @Operation(summary = "Демонстрация создания без транзакции")
    public ResponseEntity<String> createWithoutTransaction(@RequestBody PhotoSessionCreateDto createDto) {
        try {
            photoSessionService.createWithRelatedWithoutTransaction(createDto);
            return ResponseEntity.ok("Операция без транзакции выполнена успешно");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " - данные сохранены в БД");
        }
    }

    @GetMapping("/search-jpql")
    @Operation(summary = "Поиск фотосессий через JPQL")
    public ResponseEntity<List<PhotoSessionDto>> searchSessionsJpql(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String photographerName,
            @RequestParam(required = false) String phone) {

        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(clientName)
                .photographerName(photographerName)
                .phone(phone)
                .build();

        return ResponseEntity.ok(photoSessionService.getSessionsWithFiltersJpql(filter));
    }

    @GetMapping("/search-native")
    @Operation(summary = "Поиск фотосессий через Native Query")
    public ResponseEntity<List<PhotoSessionDto>> searchSessionsNative(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String photographerName,
            @RequestParam(required = false) String phone) {

        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(clientName)
                .photographerName(photographerName)
                .phone(phone)
                .build();

        return ResponseEntity.ok(photoSessionService.getSessionsWithFiltersNative(filter));
    }

    @GetMapping("/search-paginated")
    @Operation(summary = "Поиск фотосессий с пагинацией")
    public ResponseEntity<Page<PhotoSessionDto>> searchSessionsPaginated(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String photographerName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") @Parameter(description = "Номер страницы") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Размер страницы") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(clientName)
                .photographerName(photographerName)
                .phone(phone)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return ResponseEntity.ok(photoSessionService.getSessionsWithFiltersPaged(filter));
    }

    @GetMapping("/search-cached")
    @Operation(summary = "Поиск фотосессий с использованием кэша")
    public ResponseEntity<Page<PhotoSessionDto>> searchSessionsCached(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String photographerName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(clientName)
                .photographerName(photographerName)
                .phone(phone)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(photoSessionService.getSessionsWithCache(filter));
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "Получить статистику кэша")
    public ResponseEntity<String> getCacheStats() {
        return ResponseEntity.ok("Текущий размер кэша: " + photoSessionService.getCacheSize());
    }
}