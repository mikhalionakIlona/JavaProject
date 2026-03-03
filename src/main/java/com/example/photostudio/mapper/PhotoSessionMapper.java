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

        PhotoSessionDto.PhotoSessionDtoBuilder builder = PhotoSessionDto.builder()
                .id(session.getId())
                .sessionDate(session.getDate())
                .totalPrice(session.getTotalPrice());

        if (session.getClient() != null) {
            builder.clientId(session.getClient().getId())
                    .clientName(session.getClient().getFirstName())
                    .clientLastName(session.getClient().getLastName());
        }

        if (session.getPhotographer() != null) {
            builder.photographerId(session.getPhotographer().getId())
                    .photographerName(session.getPhotographer().getFirstName() + " "
                            + session.getPhotographer().getLastName());
        }

        if (session.getService() != null) {
            builder.serviceId(session.getService().getId())
                    .serviceName(session.getService().getServiceType().getDisplayName()); // Из Enum!
        }

        return builder.build();
    }
}