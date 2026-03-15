package com.example.photostudio.service;

import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientUpdateDto;
import com.example.photostudio.mapper.ClientMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Transactional
    public ClientDto createClient(ClientCreateDto createDto) {
        Client client = Client.builder()
                .firstName(createDto.getFirstName())
                .lastName(createDto.getLastName())
                .phone(createDto.getPhone())
                .email(createDto.getEmail())
                .build();
        return mapper.toDto(repository.save(client));
    }

    @Transactional
    public ClientDto updateClient(Long id, ClientUpdateDto updateDto) {
        return repository.findById(id)
                .map(client -> {
                    client.setFirstName(updateDto.getFirstName());
                    client.setLastName(updateDto.getLastName());
                    client.setPhone(updateDto.getPhone());
                    client.setEmail(updateDto.getEmail());
                    return mapper.toDto(repository.save(client));
                })
                .orElse(null);
    }

    @Transactional
    public boolean deleteClient(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}