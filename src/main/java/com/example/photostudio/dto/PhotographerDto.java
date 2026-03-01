package com.example.photostudio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotographerDto {
    private Long id;
    private String name;
    private String specialization;
    private String phone;
    private Double hourlyRate;
}