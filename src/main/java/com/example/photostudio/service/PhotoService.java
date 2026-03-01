package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoDto;
import com.example.photostudio.mapper.PhotoMapper;
import com.example.photostudio.model.Photo;
import com.example.photostudio.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository repository;
    private final PhotoMapper mapper;

    public List<PhotoDto> getAllPhotos() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public PhotoDto getPhotoById(Long id) {
        Photo photo = repository.findById(id).orElse(null);
        return mapper.toDto(photo);
    }

    public List<PhotoDto> getPhotosBySessionId(Long sessionId) {
        return repository.findByPhotoSessionId(sessionId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}