package com.example.photostudio.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhotoSessionUpdateDto {
    private LocalDateTime sessionDate;
    private Long serviceId;
}