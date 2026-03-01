package com.example.photostudio.mapper;

import com.example.photostudio.dto.ServiceDto;
import com.example.photostudio.model.PhotoService;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceDto toDto(PhotoService service) {
        if (service == null) {
            return null;
        }

        return ServiceDto.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .build();
    }
}