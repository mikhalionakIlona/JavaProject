package com.example.photostudio.dto;

import lombok.Data;

@Data
public class PhotographerCreateDto {
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phone;
    private Double hourlyRate;
}