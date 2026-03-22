package com.example.photostudio.dto.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ClientBulkCreateDto {
    @NotEmpty(message = "Список клиентов не может быть пустым")
    @Size(max = 10, message = "Нельзя создать более 10 клиентов за раз")
    @Valid
    private List<ClientCreateDto> clients;
}
