package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoDto;
import com.example.photostudio.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService service;

    @GetMapping
    public ResponseEntity<List<PhotoDto>> getAllPhotos() {
        return ResponseEntity.ok(service.getAllPhotos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoDto> getPhotoById(@PathVariable Long id) {
        PhotoDto photo = service.getPhotoById(id);
        return photo == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(photo);
    }

    @GetMapping("/by-session/{sessionId}")
    public ResponseEntity<List<PhotoDto>> getPhotosBySessionId(@PathVariable Long sessionId) {
        return ResponseEntity.ok(service.getPhotosBySessionId(sessionId));
    }

    @PostMapping
    public ResponseEntity<PhotoDto> createPhoto(@RequestParam String fileName,
                                                @RequestParam String filePath,
                                                @RequestParam Long sessionId) {
        return ResponseEntity.ok(service.createPhoto(fileName, filePath, sessionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        return service.deletePhoto(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}