package com.example.photostudio.mapper;

import com.example.photostudio.dto.PhotoDto;
import com.example.photostudio.model.Photo;
import org.springframework.stereotype.Component;

@Component
public class PhotoMapper {

    public PhotoDto toDto(Photo photo) {
        if (photo == null) {
            return null;
        }

        return PhotoDto.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .filePath(photo.getFilePath())
                .uploadDate(photo.getUploadDate())
                .sessionId(photo.getPhotoSession() != null ? photo.getPhotoSession().getId() : null)
                .build();
    }
}