package com.example.photostudio.mapper;

import com.example.photostudio.dto.PhotographerDto;
import com.example.photostudio.model.Photographer;
import org.springframework.stereotype.Component;

@Component
public class PhotographerMapper {

    public PhotographerDto toDto(Photographer photographer) {
        if (photographer == null) {
            return null;
        }

        return PhotographerDto.builder()
                .id(photographer.getId())
                .name(photographer.getName())
                .specialization(photographer.getSpecialization())
                .phone(photographer.getPhone())
                .hourlyRate(photographer.getHourlyRate())
                .build();
    }
}