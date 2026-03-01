package com.example.photostudio.mapper;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.model.PhotoSession;
import org.springframework.stereotype.Component;

@Component
public class PhotoSessionMapper {

    public PhotoSessionDto toDto(PhotoSession session) {
        if (session == null) {
            return null;
        }

        return PhotoSessionDto.builder()
                .id(session.getId())
                .clientName(session.getClientName())
                .clientLastName(session.getClientLastName())
                .clientPhone(session.getClientPhone())
                .photoSessionDate(session.getPhotoSessionDate())
                .price(session.getPrice())
                .photographer(session.getPhotographer())
                .status(session.getStatus())
                .clientId(session.getClient() != null ? session.getClient().getId() : null)
                .photographerId(session.getPhotographerEntity() != null ? session.getPhotographerEntity().getId() :
                        null)
                .serviceId(session.getService() != null ? session.getService().getId() : null)
                .build();
    }

    public PhotoSession toEntity(PhotoSessionDto dto) {
        if (dto == null) {
            return null;
        }

        return PhotoSession.builder()
                .clientName(dto.getClientName())
                .clientLastName(dto.getClientLastName())
                .clientPhone(dto.getClientPhone())
                .photoSessionDate(dto.getPhotoSessionDate())
                .price(dto.getPrice())
                .photographer(dto.getPhotographer())
                .status(dto.getStatus())
                .build();
    }

    public void updateEntity(PhotoSession session, PhotoSessionDto dto) {
        if (session == null || dto == null) {
            return;
        }

        session.setClientName(dto.getClientName());
        session.setClientLastName(dto.getClientLastName());
        session.setClientPhone(dto.getClientPhone());
        session.setPhotoSessionDate(dto.getPhotoSessionDate());
        session.setPrice(dto.getPrice());
        session.setPhotographer(dto.getPhotographer());
        session.setStatus(dto.getStatus());
    }
}