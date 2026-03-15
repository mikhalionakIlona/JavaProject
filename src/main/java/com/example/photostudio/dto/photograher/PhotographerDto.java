package com.example.photostudio.dto.photograher;

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
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phone;
    private Double hourlyRate;
}