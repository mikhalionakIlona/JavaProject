package com.example.photostudio.service;

import com.example.photostudio.dto.PhotographerDto;
import com.example.photostudio.dto.PhotographerCreateDto;
import com.example.photostudio.dto.PhotographerUpdateDto;
import com.example.photostudio.mapper.PhotographerMapper;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Transactional
    public PhotographerDto createPhotographer(PhotographerCreateDto createDto) {
        Photographer photographer = Photographer.builder()
                .firstName(createDto.getFirstName())
                .lastName(createDto.getLastName())
                .patronymic(createDto.getPatronymic())
                .phone(createDto.getPhone())
                .hourlyRate(createDto.getHourlyRate())
                .build();
        return mapper.toDto(repository.save(photographer));
    }

    @Transactional
    public PhotographerDto updatePhotographer(Long id, PhotographerUpdateDto updateDto) {
        return repository.findById(id)
                .map(photographer -> {
                    photographer.setFirstName(updateDto.getFirstName());
                    photographer.setLastName(updateDto.getLastName());
                    photographer.setPatronymic(updateDto.getPatronymic());
                    photographer.setPhone(updateDto.getPhone());
                    photographer.setHourlyRate(updateDto.getHourlyRate());
                    return mapper.toDto(repository.save(photographer));
                })
                .orElse(null);
    }

    @Transactional
    public boolean deletePhotographer(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}