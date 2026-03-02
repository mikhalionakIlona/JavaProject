package com.example.photostudio.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhotoSessionCreateDto {
    private LocalDateTime sessionDate;
    private Long clientId;
    private Long photographerId;
    private Long serviceId;
}