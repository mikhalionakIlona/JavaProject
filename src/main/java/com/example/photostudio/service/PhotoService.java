package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoDto;
import com.example.photostudio.mapper.PhotoMapper;
import com.example.photostudio.model.Photo;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.PhotoRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository repository;
    private final PhotoMapper mapper;
    private final PhotoSessionRepository photoSessionRepository;

    private static final String SESSION_NOT_FOUND = "PhotoSession not found";

    public List<PhotoDto> getAllPhotos() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public PhotoDto getPhotoById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    public List<PhotoDto> getPhotosBySessionId(Long sessionId) {
        return repository.findByPhotoSessionId(sessionId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public PhotoDto createPhoto(String fileName, String filePath, Long sessionId) {
        PhotoSession session = photoSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException(SESSION_NOT_FOUND + " with id: " + sessionId));

        Photo photo = Photo.builder()
                .fileName(fileName)
                .filePath(filePath)
                .uploadDate(LocalDateTime.now())
                .photoSession(session)
                .build();

        return mapper.toDto(repository.save(photo));
    }

    @Transactional
    public boolean deletePhoto(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}