package com.example.photostudio.dto.service;

import com.example.photostudio.model.ServiceType;
import lombok.Data;

@Data
public class ServiceUpdateDto {
    private ServiceType serviceType;
}