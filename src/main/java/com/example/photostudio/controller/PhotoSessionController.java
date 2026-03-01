package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.service.PhotoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public List<PhotoSessionDto> getAllPhotoSessions(
            @RequestParam(required = false) String clientName) {
        return photoSessionService.getAllPhotoSessions(clientName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> getPhotoSessionById(@PathVariable Long id) {
        PhotoSessionDto dto = photoSessionService.getPhotoSessionById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public PhotoSessionDto createPhotoSession(@RequestBody PhotoSessionDto dto) {
        return photoSessionService.createPhotoSession(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> updatePhotoSession(
            @PathVariable Long id, @RequestBody PhotoSessionDto dto) {
        PhotoSessionDto updated = photoSessionService.updatePhotoSession(id, dto);
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
        return ResponseEntity.ok("Решение с @EntityGraph продемонстрировано. Проверьте логи.");
    }

    @PostMapping("/demonstrate/with-transaction")
    public ResponseEntity<PhotoSessionDto> demonstrateWithTransaction(
            @RequestBody PhotoSessionDto dto,
            @RequestParam Long clientId,
            @RequestParam Long photographerId,
            @RequestParam Long serviceId) {
        try {
            PhotoSessionDto result = photoSessionService.createWithRelatedWithTransaction(
                    dto, clientId, photographerId, serviceId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}