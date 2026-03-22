package com.example.photostudio.service;

import com.example.photostudio.dto.client.ClientCreateDto;
import com.example.photostudio.dto.client.ClientDto;
import com.example.photostudio.dto.client.ClientUpdateDto;
import com.example.photostudio.mapper.ClientMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final String FIRST_NAME = "Иван";
    private static final String LAST_NAME = "Петров";
    private static final String UPDATED_LAST_NAME = "Сидоров";
    private static final String PHONE = "+71234567890";
    private static final String EMAIL = "ivan@mail.com";

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private ClientCreateDto clientCreateDto;
    private Client client;
    private ClientDto clientDto;
    private ClientUpdateDto clientUpdateDto;

    @BeforeEach
    void setUp() {
        clientCreateDto = new ClientCreateDto();
        clientCreateDto.setFirstName(FIRST_NAME);
        clientCreateDto.setLastName(LAST_NAME);
        clientCreateDto.setPhone(PHONE);
        clientCreateDto.setEmail(EMAIL);

        clientUpdateDto = new ClientUpdateDto();
        clientUpdateDto.setFirstName("Петр");
        clientUpdateDto.setLastName(UPDATED_LAST_NAME);

        client = Client.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phone(PHONE)
                .email(EMAIL)
                .build();

        clientDto = ClientDto.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phone(PHONE)
                .email(EMAIL)
                .build();
    }

    @Test
    void getAllClientsShouldReturnListOfClients() {
        List<Client> clients = List.of(client);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        List<ClientDto> result = clientService.getAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientByIdWhenClientExistsShouldReturnClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getClientById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClientByIdWhenClientNotExistsShouldReturnNull() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        ClientDto result = clientService.getClientById(999L);

        assertThat(result).isNull();
        verify(clientRepository, times(1)).findById(999L);
    }

    @Test
    void createClientShouldReturnCreatedClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        ClientDto result = clientService.createClient(clientCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientWhenClientExistsShouldReturnUpdatedClient() {
        Client updatedClient = Client.builder()
                .id(1L)
                .firstName("Петр")
                .lastName(UPDATED_LAST_NAME)
                .phone(PHONE)
                .email(EMAIL)
                .build();

        ClientDto updatedDto = ClientDto.builder()
                .id(1L)
                .firstName("Петр")
                .lastName(UPDATED_LAST_NAME)
                .phone(PHONE)
                .email(EMAIL)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.toDto(updatedClient)).thenReturn(updatedDto);

        ClientDto result = clientService.updateClient(1L, clientUpdateDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Петр");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientWhenClientNotExistsShouldReturnNull() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        ClientDto result = clientService.updateClient(999L, clientUpdateDto);

        assertThat(result).isNull();
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteClientWhenClientExistsShouldReturnTrue() {
        when(clientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(1L);

        boolean result = clientService.deleteClient(1L);

        assertThat(result).isTrue();
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteClientWhenClientNotExistsShouldReturnFalse() {
        when(clientRepository.existsById(999L)).thenReturn(false);

        boolean result = clientService.deleteClient(999L);

        assertThat(result).isFalse();
        verify(clientRepository, never()).deleteById(anyLong());
    }

    @Test
    void createClientsBulkShouldReturnListOfCreatedClients() {
        List<ClientCreateDto> createDto = Arrays.asList(clientCreateDto, clientCreateDto);
        List<Client> clients = Arrays.asList(client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        List<ClientDto> result = clientService.createClientsBulk(createDto);

        assertThat(result).hasSize(2);
        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithTransactionWhenMoreThan3ShouldThrowException() {
        List<ClientCreateDto> createDtos = Arrays.asList(
                clientCreateDto, clientCreateDto, clientCreateDto, clientCreateDto
        );
        List<Client> clients = Arrays.asList(client, client, client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);

        assertThatThrownBy(() -> clientService.createClientsBulkWithTransaction(createDtos))
                .isInstanceOf(RuntimeException.class);

        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithoutTransactionWhenMoreThan3ShouldThrowExceptionButDataSaved() {
        List<ClientCreateDto> createDto = Arrays.asList(
                clientCreateDto, clientCreateDto, clientCreateDto, clientCreateDto
        );
        List<Client> clients = Arrays.asList(client, client, client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);

        assertThatThrownBy(() -> clientService.createClientsBulkWithoutTransaction(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Данные уже сохранены");
        verify(clientRepository, times(1)).saveAll(anyList());
    }
}