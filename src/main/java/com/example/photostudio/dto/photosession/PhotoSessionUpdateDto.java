package com.example.photostudio.dto.photosession;

import jakarta.validation.constraints.Future;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhotoSessionUpdateDto {
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime date;

    private Long serviceId;
}