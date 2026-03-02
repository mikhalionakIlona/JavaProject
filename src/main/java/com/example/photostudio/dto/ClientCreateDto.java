package com.example.photostudio.dto;

import lombok.Data;

@Data
public class ClientCreateDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
}