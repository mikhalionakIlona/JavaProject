package com.example.photostudio.dto;

import com.example.photostudio.model.ServiceType;
import lombok.Data;

@Data
public class ServiceCreateDto {
    private ServiceType serviceType;
}