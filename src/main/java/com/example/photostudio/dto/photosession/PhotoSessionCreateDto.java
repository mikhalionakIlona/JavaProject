package com.example.photostudio.dto.photosession;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhotoSessionCreateDto {
    @NotNull(message = "Дата обязательна")
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime date;

    @NotNull(message = "ID клиента обязателен")
    private Long clientId;

    @NotNull(message = "ID фотографа обязателен")
    private Long photographerId;

    @NotNull(message = "ID услуги обязателен")
    private Long serviceId;
}