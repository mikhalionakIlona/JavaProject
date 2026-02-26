package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoSessionRequestDto;
import com.example.photostudio.dto.PhotoSessionResponseDto;
import com.example.photostudio.service.PhotoSessionService;
import java.util.List;
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


@RestController
@RequestMapping("/api/photoSession")
@RequiredArgsConstructor
public final class PhotoSessionController {
     private final PhotoSessionService service;

    @GetMapping
    public ResponseEntity<List<PhotoSessionResponseDto>> getPhotosessions(
            @RequestParam(value = "client", required = false) String clientName) {
        List<PhotoSessionResponseDto> photoSessions = (clientName != null &&
            !clientName.trim().isEmpty())
                ? service.getPhotoSessionByClientName(clientName)
                : service.getAllPhotoSessions();

        return ResponseEntity.ok(photoSessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoSessionResponseDto> getPhotoSessionById(@PathVariable Long id) {
        PhotoSessionResponseDto photoSession = service.getPhotoSessionById(id);

        return photoSession == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(photoSession);
    }

    @PostMapping
    public ResponseEntity<PhotoSessionResponseDto> createPhotoSession(
            @RequestBody PhotoSessionRequestDto requestDto) {
        PhotoSessionResponseDto createdPhotoSession = service.createPhotoSession(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPhotoSession);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoSessionResponseDto> updatePhotoSession(
            @PathVariable Long id,
            @RequestBody PhotoSessionRequestDto requestDto) {
        PhotoSessionResponseDto updatedPhotoSession = service.updatePhotoSession(id, requestDto);
        return updatedPhotoSession == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedPhotoSession);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhotoSession(@PathVariable Long id) {
        boolean deleted = service.deletePhotoSession(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}