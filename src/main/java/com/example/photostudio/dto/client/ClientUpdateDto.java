package com.example.photostudio.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientUpdateDto {

    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Pattern(regexp = "^\\\\+?\\\\d{10,15}$", message = "Неверный формат телефона")
    private String phone;

    @Email(message = "Неверный формат email")
    private String email;
}