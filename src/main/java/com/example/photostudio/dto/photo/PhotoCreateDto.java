package com.example.photostudio.dto.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhotoCreateDto {
    @NotBlank(message = "Имя файла обязательно")
    private String fileName;

    @NotBlank(message = "Путь к файлу обязателен")
    private String filePath;

    @NotNull(message = "ID сессии обязателен")
    private Long sessionId;
}
