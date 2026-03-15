package com.example.photostudio.dto.service;

import com.example.photostudio.model.ServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceCreateDto {
    @NotNull(message = "Тип услуги обязателен")
    private ServiceType serviceType;
}