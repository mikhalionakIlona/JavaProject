package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotoSessionAdminResponseDto;
import com.example.photostudio.dto.PhotoSessionRequestDto;
import com.example.photostudio.dto.PhotoSessionResponseDto;
import com.example.photostudio.service.PhotoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/photoSession")
@RequiredArgsConstructor
public class PhotoSessionController {
    private final PhotoSessionService service;

    @GetMapping
    public ResponseEntity<List<PhotoSessionResponseDto>> getPhotosessions(
            @RequestParam(value = "client", required = false) String clientName) {
        List<PhotoSessionResponseDto> photoSessions = 
            (clientName != null && !clientName.trim().isEmpty())
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

    @GetMapping("/admin")
    public ResponseEntity<List<PhotoSessionAdminResponseDto>> getPhotoSessionFormAdmin(
            @RequestParam(value = "clientName", required = false) String clientName,
            @RequestParam(value = "clientLastName", required = false) String clientLastName) {
        
        List<PhotoSessionAdminResponseDto> photoSessions;
        
        if ((clientName != null && !clientName.trim().isEmpty()) 
                || (clientLastName != null && !clientLastName.trim().isEmpty())) {
            photoSessions = service.getPhotoSessionForAdminClientFullName(
                clientLastName, clientName);
        } else {
            photoSessions = service.getAllPhotoSessionForAdmin();
        }
        
        return ResponseEntity.ok(photoSessions);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<PhotoSessionAdminResponseDto> getPhotoSessionFormAdminById(
            @PathVariable Long id) {
        PhotoSessionAdminResponseDto photoSession = service.getPhotoSessionForAdminById(id);
        
        return photoSession == null 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(photoSession);
    }

    @PostMapping("/admin")
    public ResponseEntity<PhotoSessionResponseDto> createPhotoSession(
            @RequestBody PhotoSessionRequestDto requestDto) {
        PhotoSessionResponseDto createdPhotoSession = service.createPhotoSession(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPhotoSession);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<PhotoSessionAdminResponseDto> updatePhotoSession(
            @PathVariable Long id,
            @RequestBody PhotoSessionAdminResponseDto adminDto) {
        
        if (!id.equals(adminDto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        PhotoSessionAdminResponseDto photoSession = 
            service.updatePhotoSessionFormAdmin(id, adminDto);
        
        return photoSession == null 
            ? ResponseEntity.badRequest().build()
            : ResponseEntity.ok(photoSession);
    }

    @PatchMapping("/admin/{id}")
    public ResponseEntity<PhotoSessionResponseDto> partialUpdatePhotoSession(
            @PathVariable Long id,
            @RequestBody PhotoSessionRequestDto requestDto) {
        
        PhotoSessionResponseDto updatePhotoSession = service.updatePhotoSession(id, requestDto);
        
        return updatePhotoSession == null 
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(updatePhotoSession);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deletePhotoSession(@PathVariable Long id) {
        boolean deleted = service.deletePhotoSession(id);
        
        return deleted 
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}