package com.example.photostudio.service;

import com.example.photostudio.dto.ServiceDto;
import com.example.photostudio.mapper.ServiceMapper;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceService {
    private final ServiceRepository repository;
    private final ServiceMapper mapper;

    public List<ServiceDto> getAllServices() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ServiceDto getServiceById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }
}