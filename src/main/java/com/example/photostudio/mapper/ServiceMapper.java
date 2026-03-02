package com.example.photostudio.mapper;

import com.example.photostudio.dto.ServiceDto;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.model.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceDto toDto(PhotoService service) {
        if (service == null) {
            return null;
        }

        return ServiceDto.builder()
                .id(service.getId())
                .serviceType(service.getServiceType())
                .name(service.getServiceType().getDisplayName())
                .build();
    }

    public PhotoService toEntity(ServiceType serviceType) {
        if (serviceType == null) {
            return null;
        }

        return PhotoService.builder()
                .serviceType(serviceType)
                .build();
    }
}