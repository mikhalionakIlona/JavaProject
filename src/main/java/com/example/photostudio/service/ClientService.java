package com.example.photostudio.service;

import com.example.photostudio.dto.ClientDto;
import com.example.photostudio.mapper.ClientMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repository;
    private final ClientMapper mapper;

    public List<ClientDto> getAllClients() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ClientDto getClientById(Long id) {
        Client client = repository.findById(id).orElse(null);
        return mapper.toDto(client);
    }
}