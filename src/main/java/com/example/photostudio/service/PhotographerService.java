package com.example.photostudio.service;

import com.example.photostudio.dto.PhotographerDto;
import com.example.photostudio.mapper.PhotographerMapper;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotographerService {
    private final PhotographerRepository repository;
    private final PhotographerMapper mapper;

    public List<PhotographerDto> getAllPhotographers() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public PhotographerDto getPhotographerById(Long id) {
        Photographer photographer = repository.findById(id).orElse(null);
        return mapper.toDto(photographer);
    }
}