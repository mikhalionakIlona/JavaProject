package com.example.photostudio.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhotoSessionUpdateDto {
    private LocalDateTime date;
    private Long serviceId;
}