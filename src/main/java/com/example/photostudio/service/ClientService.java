package com.example.photostudio.service;

import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientUpdateDto;
import com.example.photostudio.mapper.ClientMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private static final int MAX_BULK_SIZE = 3;

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

    public ClientDto getClientByEmail(String email) {
        return repository.findAll()
                .stream()
                .filter(client -> client.getEmail() != null)
                .filter(client -> client.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .map(mapper::toDto)
                .orElse(null);
    }

    public List<ClientDto> getClientsByPhonePattern(String pattern) {
        return repository.findAll()
                .stream()
                .filter(client -> client.getPhone() != null)
                .filter(client -> client.getPhone().contains(pattern))
                .map(mapper::toDto)
                .toList();
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
                    if (updateDto.getFirstName() != null) {
                        client.setFirstName(updateDto.getFirstName());
                    }
                    if (updateDto.getLastName() != null) {
                        client.setLastName(updateDto.getLastName());
                    }
                    if (updateDto.getPhone() != null) {
                        client.setPhone(updateDto.getPhone());
                    }
                    if (updateDto.getEmail() != null) {
                        client.setEmail(updateDto.getEmail());
                    }
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

    @Transactional
    public List<ClientDto> createClientsBulk(List<ClientCreateDto> createDto) {
        log.info("Массовое создание {} клиентов", createDto.size());
        List<Client> clients = createDto.stream()
                .map(this::convertToEntity)
                .toList();
        List<Client> savedClients = repository.saveAll(clients);
        return savedClients.stream()
                .map(mapper::toDto)
                .toList();
    }

    private Client convertToEntity(ClientCreateDto dto) {
        return Client.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .build();
    }

    @Transactional
    public List<ClientDto> createClientsBulkWithTransaction(List<ClientCreateDto> createDto) {
        log.info("Создание клиентов с транзакцией");

        List<Client> clients = createDto.stream()
                .map(this::convertToEntity)
                .toList();
        List<Client> savedClients = repository.saveAll(clients);

        if (createDto.size() > MAX_BULK_SIZE) {
            throw new NoSuchElementException(
                    "Превышен лимит в " + MAX_BULK_SIZE + " клиентов. "
                            + "Транзакция будет откачена"
            );
        }

        return savedClients.stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ClientDto> createClientsBulkWithoutTransaction(List<ClientCreateDto> createDto) {
        log.info("Создание клиентов без транзакции");

        List<Client> clients = createDto.stream()
                .map(this::convertToEntity)
                .toList();
        List<Client> savedClients = repository.saveAll(clients);

        if (createDto.size() > MAX_BULK_SIZE) {
            throw new NoSuchElementException(
                    "Превышен лимит в " + MAX_BULK_SIZE + " клиентов. "
                            + "Данные уже сохранены в БД"
            );
        }

        return savedClients.stream()
                .map(mapper::toDto)
                .toList();
    }
}