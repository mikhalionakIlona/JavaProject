package com.example.photostudio.dto.photographer;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhotographerUpdateDto {
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Size(max = 50, message = "Отчество должно содержать не более 50 символов")
    private String patronymic;

    @Pattern(regexp = "^\\+375\\d{9}$", message = "Неверный формат телефона")
    private String phone;

    @Positive(message = "Почасовая ставка должна быть положительной")
    private Double hourlyRate;
}