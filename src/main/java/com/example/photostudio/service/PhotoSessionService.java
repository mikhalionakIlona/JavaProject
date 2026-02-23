package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionAdminResponseDto;
import com.example.photostudio.dto.PhotoSessionRequestDto;
import com.example.photostudio.dto.PhotoSessionResponseDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.PhotoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private final PhotoSessionMapper mapper;
    private final PhotoSessionRepository repository;

    public List<PhotoSessionResponseDto> getAllPhotoSessions() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public PhotoSessionResponseDto getPhotoSessionById(Long id) {
        PhotoSession photoSession = repository.findById(id);
        return mapper.toResponseDto(photoSession);
    }

    public List<PhotoSessionResponseDto> getPhotoSessionByClientName(String clientName) {
        return repository.findByClientName(clientName)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public List<PhotoSessionAdminResponseDto> getAllPhotoSessionForAdmin() {
        return repository.findAll()
                .stream()
                .map(mapper::toAdminResponseDto)
                .toList();
    }

    public PhotoSessionAdminResponseDto getPhotoSessionForAdminById(Long id) {
        PhotoSession photoSession = repository.findById(id);
        return mapper.toAdminResponseDto(photoSession);
    }

    public List<PhotoSessionAdminResponseDto> 
        getPhotoSessionForAdminClientFullName(String clientLastName, String clientName) {
        return repository.findByFirstNameAndLastName(clientName, clientLastName)
                .stream()
                .map(mapper::toAdminResponseDto)
                .toList();
    }

    public boolean deletePhotoSession(Long id) {
        if (repository.existById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public PhotoSessionResponseDto createPhotoSession(PhotoSessionRequestDto requestDto) {
        PhotoSession photoSession = mapper.toEntity(requestDto);
        PhotoSession savedPhotoSession = repository.save(photoSession);
        return mapper.toResponseDto(savedPhotoSession);
    }

    public PhotoSessionResponseDto updatePhotoSession(Long id, PhotoSessionRequestDto requestDto) {
        PhotoSession existingPhotoSession = repository.findById(id);
        if (existingPhotoSession == null) {
            return null;
        }

        mapper.updateEntity(existingPhotoSession, requestDto);
        PhotoSession updatePhotoSession = repository.save(existingPhotoSession);
        return mapper.toResponseDto(updatePhotoSession);
    }

    public PhotoSessionAdminResponseDto 
        updatePhotoSessionFormAdmin(Long id, PhotoSessionAdminResponseDto adminDto) {
        if (!id.equals(adminDto.getId())) {
            return null;
        }

        PhotoSession photoSession = mapper.toEntityFromAdmin(adminDto);
        PhotoSession updatePhotoSession = repository.save(photoSession);
        return mapper.toAdminResponseDto(updatePhotoSession);
    }
}