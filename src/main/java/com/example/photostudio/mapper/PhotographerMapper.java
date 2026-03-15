package com.example.photostudio.mapper;

import com.example.photostudio.dto.photograher.PhotographerDto;
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
                .firstName(photographer.getFirstName())
                .lastName(photographer.getLastName())
                .patronymic(photographer.getPatronymic())
                .phone(photographer.getPhone())
                .hourlyRate(photographer.getHourlyRate())
                .build();
    }
}