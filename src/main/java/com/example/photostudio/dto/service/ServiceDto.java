package com.example.photostudio.dto.service;

import com.example.photostudio.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {
    private Long id;
    private ServiceType serviceType;
    private String name;
}