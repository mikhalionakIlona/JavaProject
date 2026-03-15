package com.example.photostudio.dto.photograher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhotographerCreateDto {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Size(max = 50, message = "Отчество должно содержать не более 50 символов")
    private String patronymic;

    @Pattern(regexp = "^\\\\+?\\\\d{10,15}$", message = "Неверный формат телефона")
    private String phone;

    @NotNull(message = "Почасовая ставка обязательна")
    @Positive(message = "Почасовая ставка должна быть положительной")
    private Double hourlyRate;
}