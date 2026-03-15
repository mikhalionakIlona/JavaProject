package com.example.photostudio.service;

import com.example.photostudio.dto.service.ServiceDto;
import com.example.photostudio.dto.service.ServiceCreateDto;
import com.example.photostudio.dto.service.ServiceUpdateDto;
import com.example.photostudio.mapper.ServiceMapper;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public ServiceDto createService(ServiceCreateDto createDto) {
        PhotoService service = mapper.toEntity(createDto.getServiceType());
        return mapper.toDto(repository.save(service));
    }

    @Transactional
    public ServiceDto updateService(Long id, ServiceUpdateDto updateDto) {
        return repository.findById(id)
                .map(service -> {
                    service.setServiceType(updateDto.getServiceType());
                    return mapper.toDto(repository.save(service));
                })
                .orElse(null);
    }

    @Transactional
    public boolean deleteService(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}