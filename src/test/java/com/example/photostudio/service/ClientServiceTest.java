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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private static final String UPDATED_FIRST_NAME = "Петр";
    private static final String PHONE = "+375291234567";
    private static final String UPDATED_PHONE = "+375441234567";
    private static final String EMAIL = "ivan@mail.com";
    private static final String UPDATED_EMAIL = "petr@mail.com";
    private static final long ID = 1L;
    private static final long NON_EXISTENT_ID = 999L;

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
    private ClientUpdateDto partialUpdateDto;

    @BeforeEach
    void setUp() {
        clientCreateDto = new ClientCreateDto();
        clientCreateDto.setFirstName(FIRST_NAME);
        clientCreateDto.setLastName(LAST_NAME);
        clientCreateDto.setPhone(PHONE);
        clientCreateDto.setEmail(EMAIL);

        clientUpdateDto = new ClientUpdateDto();
        clientUpdateDto.setFirstName(UPDATED_FIRST_NAME);
        clientUpdateDto.setLastName(UPDATED_LAST_NAME);
        clientUpdateDto.setPhone(UPDATED_PHONE);
        clientUpdateDto.setEmail(UPDATED_EMAIL);

        partialUpdateDto = new ClientUpdateDto();
        partialUpdateDto.setLastName(UPDATED_LAST_NAME);
        partialUpdateDto.setPhone(UPDATED_PHONE);

        client = Client.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phone(PHONE)
                .email(EMAIL)
                .build();

        clientDto = ClientDto.builder()
                .id(ID)
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
        assertThat(result.get(0).getId()).isEqualTo(ID);
        assertThat(result.get(0).getFirstName()).isEqualTo(FIRST_NAME);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getAllClientsWhenNoClientsShouldReturnEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientDto> result = clientService.getAllClients();

        assertThat(result).isEmpty();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientByIdWhenClientExistsShouldReturnClient() {
        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getClientById(ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        verify(clientRepository, times(1)).findById(ID);
    }

    @Test
    void getClientByIdWhenClientNotExistsShouldReturnNull() {
        when(clientRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        ClientDto result = clientService.getClientById(NON_EXISTENT_ID);

        assertThat(result).isNull();
        verify(clientRepository, times(1)).findById(NON_EXISTENT_ID);
    }

    @Test
    void createClientShouldReturnCreatedClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        ClientDto result = clientService.createClient(clientCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void createClientShouldSaveClientWithCorrectData() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        clientService.createClient(clientCreateDto);

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClientWhenClientExistsShouldReturnUpdatedClient() {
        Client updatedClient = Client.builder()
                .id(ID)
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .phone(UPDATED_PHONE)
                .email(UPDATED_EMAIL)
                .build();

        ClientDto updatedDto = ClientDto.builder()
                .id(ID)
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .phone(UPDATED_PHONE)
                .email(UPDATED_EMAIL)
                .build();

        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.toDto(updatedClient)).thenReturn(updatedDto);

        ClientDto result = clientService.updateClient(ID, clientUpdateDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(result.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(result.getEmail()).isEqualTo(UPDATED_EMAIL);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientWithPartialDataShouldUpdateOnlyProvidedFields() {
        Client updatedClient = Client.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .phone(UPDATED_PHONE)
                .email(EMAIL)
                .build();

        ClientDto updatedDto = ClientDto.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .phone(UPDATED_PHONE)
                .email(EMAIL)
                .build();

        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.toDto(updatedClient)).thenReturn(updatedDto);

        ClientDto result = clientService.updateClient(ID, partialUpdateDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(result.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClientWhenClientNotExistsShouldReturnNull() {
        when(clientRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        ClientDto result = clientService.updateClient(NON_EXISTENT_ID, clientUpdateDto);

        assertThat(result).isNull();
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteClientWhenClientExistsShouldReturnTrue() {
        when(clientRepository.existsById(ID)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(ID);

        boolean result = clientService.deleteClient(ID);

        assertThat(result).isTrue();
        verify(clientRepository, times(1)).existsById(ID);
        verify(clientRepository, times(1)).deleteById(ID);
    }

    @Test
    void deleteClientWhenClientNotExistsShouldReturnFalse() {
        when(clientRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        boolean result = clientService.deleteClient(NON_EXISTENT_ID);

        assertThat(result).isFalse();
        verify(clientRepository, times(1)).existsById(NON_EXISTENT_ID);
        verify(clientRepository, never()).deleteById(anyLong());
    }

    @Test
    void createClientsBulkShouldReturnListOfCreatedClients() {
        List<ClientCreateDto> createDtos = Arrays.asList(clientCreateDto, clientCreateDto);
        List<Client> clients = Arrays.asList(client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        List<ClientDto> result = clientService.createClientsBulk(createDtos);

        assertThat(result).hasSize(2);
        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithEmptyListShouldReturnEmptyList() {
        List<ClientCreateDto> createDtos = List.of();

        when(clientRepository.saveAll(anyList())).thenReturn(List.of());

        List<ClientDto> result = clientService.createClientsBulk(createDtos);

        assertThat(result).isEmpty();
        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithTransactionWhenLessOrEqual3ShouldSucceed() {
        List<ClientCreateDto> createDtos = Arrays.asList(
                clientCreateDto, clientCreateDto, clientCreateDto
        );
        List<Client> clients = Arrays.asList(client, client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        List<ClientDto> result = clientService.createClientsBulkWithTransaction(createDtos);

        assertThat(result).hasSize(3);
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
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Превышен лимит")
                .hasMessageContaining("Транзакция будет откачена");

        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithoutTransactionWhenLessOrEqual3ShouldSucceed() {
        List<ClientCreateDto> createDtos = Arrays.asList(
                clientCreateDto, clientCreateDto, clientCreateDto
        );
        List<Client> clients = Arrays.asList(client, client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        List<ClientDto> result = clientService.createClientsBulkWithoutTransaction(createDtos);

        assertThat(result).hasSize(3);
        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createClientsBulkWithoutTransactionWhenMoreThan3ShouldThrowExceptionButDataSaved() {
        List<ClientCreateDto> createDtos = Arrays.asList(
                clientCreateDto, clientCreateDto, clientCreateDto, clientCreateDto
        );
        List<Client> clients = Arrays.asList(client, client, client, client);

        when(clientRepository.saveAll(anyList())).thenReturn(clients);

        assertThatThrownBy(() -> clientService.createClientsBulkWithoutTransaction(createDtos))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Превышен лимит")
                .hasMessageContaining("Данные уже сохранены");

        verify(clientRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getClientByEmailWhenExistsShouldReturnClient() {
        List<Client> clients = List.of(client);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getClientByEmail(EMAIL);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientByEmailWhenNotExistsShouldReturnNull() {
        List<Client> clients = List.of(client);
        when(clientRepository.findAll()).thenReturn(clients);

        ClientDto result = clientService.getClientByEmail("notexists@mail.com");

        assertThat(result).isNull();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientByEmailWhenEmailIsNullShouldReturnNull() {
        Client clientWithNullEmail = Client.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phone(PHONE)
                .email(null)
                .build();

        when(clientRepository.findAll()).thenReturn(List.of(clientWithNullEmail));

        ClientDto result = clientService.getClientByEmail(EMAIL);

        assertThat(result).isNull();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientByEmailWhenMultipleClientsShouldReturnFirstMatch() {
        Client client2 = Client.builder()
                .id(2L)
                .firstName("Петр")
                .lastName("Сидоров")
                .phone(PHONE)
                .email(EMAIL)
                .build();

        ClientDto clientDto2 = ClientDto.builder()
                .id(2L)
                .firstName("Петр")
                .lastName("Сидоров")
                .phone(PHONE)
                .email(EMAIL)
                .build();

        List<Client> clients = Arrays.asList(client, client2);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        when(clientMapper.toDto(client2)).thenReturn(clientDto2);

        ClientDto result = clientService.getClientByEmail(EMAIL);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ID);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientsByPhonePatternShouldReturnFilteredList() {
        List<Client> clients = List.of(client);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        List<ClientDto> result = clientService.getClientsByPhonePattern("123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhone()).contains("123");
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientsByPhonePatternWhenNoMatchShouldReturnEmptyList() {
        List<Client> clients = List.of(client);
        when(clientRepository.findAll()).thenReturn(clients);

        List<ClientDto> result = clientService.getClientsByPhonePattern("999");

        assertThat(result).isEmpty();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientsByPhonePatternWhenPhoneIsNullShouldSkip() {
        Client clientWithNullPhone = Client.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .phone(null)
                .email(EMAIL)
                .build();

        when(clientRepository.findAll()).thenReturn(List.of(clientWithNullPhone));

        List<ClientDto> result = clientService.getClientsByPhonePattern("123");

        assertThat(result).isEmpty();
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getClientsByPhonePatternShouldReturnMultipleMatches() {
        Client client2 = Client.builder()
                .id(2L)
                .firstName("Петр")
                .lastName("Сидоров")
                .phone("+375441234567")
                .email("petr@mail.com")
                .build();

        ClientDto clientDto2 = ClientDto.builder()
                .id(2L)
                .firstName("Петр")
                .lastName("Сидоров")
                .phone("+375441234567")
                .email("petr@mail.com")
                .build();

        List<Client> clients = Arrays.asList(client, client2);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        when(clientMapper.toDto(client2)).thenReturn(clientDto2);

        List<ClientDto> result = clientService.getClientsByPhonePattern("123");

        assertThat(result).hasSize(2);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void convertToEntityShouldMapAllFields() {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setPhone(PHONE);
        dto.setEmail(EMAIL);

        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDto);

        ClientDto result = clientService.createClient(dto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(LAST_NAME);
        assertThat(result.getPhone()).isEqualTo(PHONE);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
    }
}