package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.dto.PhotoSessionCreateDto;
import com.example.photostudio.dto.PhotoSessionUpdateDto;
import com.example.photostudio.dto.PhotoSessionFilterDto;
import com.example.photostudio.service.PhotoSessionService;
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
public class PhotoSessionController {
    private final PhotoSessionService photoSessionService;

    @GetMapping
    public ResponseEntity<List<PhotoSessionDto>> getAllPhotoSessions() {
        return ResponseEntity.ok(photoSessionService.getAllPhotoSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> getPhotoSessionById(@PathVariable Long id) {
        PhotoSessionDto dto = photoSessionService.getPhotoSessionById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PhotoSessionDto>> getPhotoSessionsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(photoSessionService.getPhotoSessionsByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<PhotoSessionDto> createPhotoSession(@RequestBody PhotoSessionCreateDto createDto) {
        return ResponseEntity.ok(photoSessionService.createPhotoSession(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> updatePhotoSession(
            @PathVariable Long id, @RequestBody PhotoSessionUpdateDto updateDto) {
        PhotoSessionDto updated = photoSessionService.updatePhotoSession(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhotoSession(@PathVariable Long id) {
        return photoSessionService.deletePhotoSession(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/demonstrate/nplus1")
    public ResponseEntity<String> demonstrateNPlus1() {
        photoSessionService.demonstrateNPlus1Problem();
        return ResponseEntity.ok("N+1 проблема продемонстрирована. Проверьте логи.");
    }

    @GetMapping("/demonstrate/entity-graph")
    public ResponseEntity<String> demonstrateEntityGraph() {
        photoSessionService.demonstrateEntityGraphSolution();
        return ResponseEntity.ok("Решение с EntityGraph продемонстрировано. Проверьте логи.");
    }

    @PostMapping("/demonstrate/with-transaction")
    public ResponseEntity<String> createWithTransaction(@RequestBody PhotoSessionCreateDto createDto) {
        try {
            photoSessionService.createWithRelatedWithTransaction(createDto);
            return ResponseEntity.ok("Операция с транзакцией выполнена успешно");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " - транзакция откачена");
        }
    }

    @PostMapping("/demonstrate/without-transaction")
    public ResponseEntity<String> createWithoutTransaction(@RequestBody PhotoSessionCreateDto createDto) {
        try {
            photoSessionService.createWithRelatedWithoutTransaction(createDto);
            return ResponseEntity.ok("Операция без транзакции выполнена успешно");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " - данные сохранены в БД");
        }
    }

    @GetMapping("/search-jpql")
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
    public ResponseEntity<Page<PhotoSessionDto>> searchSessionsPaginated(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String photographerName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
    public ResponseEntity<String> getCacheStats() {
        return ResponseEntity.ok("Текущий размер кэша: " + photoSessionService.getCacheSize());
    }
}