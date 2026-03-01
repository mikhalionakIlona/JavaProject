package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.service.PhotoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class PhotoSessionController {
    private final PhotoSessionService service;

    // GET с @RequestParam
    @GetMapping
    public ResponseEntity<List<PhotoSessionDto>> getSessions(
            @RequestParam(value = "client", required = false) String clientName) {
        return ResponseEntity.ok(service.getAllPhotoSessions(clientName));
    }

    // GET с @PathVariable
    @GetMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> getSessionById(@PathVariable Long id) {
        PhotoSessionDto session = service.getPhotoSessionById(id);
        return session == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(session);
    }

    // CRUD
    @PostMapping
    public ResponseEntity<PhotoSessionDto> createSession(@RequestBody PhotoSessionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPhotoSession(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoSessionDto> updateSession(@PathVariable Long id,
                                                         @RequestBody PhotoSessionDto dto) {
        PhotoSessionDto updated = service.updatePhotoSession(id, dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        return service.deletePhotoSession(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Демонстрация N+1 и решений
    @GetMapping("/nplus1")
    public ResponseEntity<List<PhotoSessionDto>> getWithNPlus1() {
        return ResponseEntity.ok(service.getAllWithNPlus1());
    }

    @GetMapping("/join-fetch")
    public ResponseEntity<List<PhotoSessionDto>> getWithJoinFetch() {
        return ResponseEntity.ok(service.getAllWithJoinFetch());
    }

    @GetMapping("/entity-graph")
    public ResponseEntity<List<PhotoSessionDto>> getWithEntityGraph() {
        return ResponseEntity.ok(service.getAllWithEntityGraph());
    }

    // Демонстрация транзакций
    @PostMapping("/no-tx")
    public ResponseEntity<PhotoSessionDto> createNoTransaction(@RequestBody PhotoSessionDto dto,
                                                               @RequestParam Long clientId,
                                                               @RequestParam Long photographerId,
                                                               @RequestParam Long serviceId) {
        return ResponseEntity.ok(service.createWithRelatedNoTransaction(dto, clientId, photographerId, serviceId));
    }

    @PostMapping("/with-tx")
    public ResponseEntity<PhotoSessionDto> createWithTransaction(@RequestBody PhotoSessionDto dto,
                                                                 @RequestParam Long clientId,
                                                                 @RequestParam Long photographerId,
                                                                 @RequestParam Long serviceId) {
        return ResponseEntity.ok(service.createWithRelatedWithTransaction(dto, clientId, photographerId, serviceId));
    }
}