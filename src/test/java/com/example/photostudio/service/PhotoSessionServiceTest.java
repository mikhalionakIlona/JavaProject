package com.example.photostudio.service;

import com.example.photostudio.cache.PhotoSessionCache;
import com.example.photostudio.cache.PhotoSessionQueryKey;
import com.example.photostudio.dto.photosession.PhotoSessionCreateDto;
import com.example.photostudio.dto.photosession.PhotoSessionDto;
import com.example.photostudio.dto.photosession.PhotoSessionFilterDto;
import com.example.photostudio.dto.photosession.PhotoSessionUpdateDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.model.ServiceType;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhotoSessionServiceTest {

    private static final String CLIENT_FIRST_NAME = "Иван";
    private static final String CLIENT_LAST_NAME = "Петров";
    private static final String CLIENT_PHONE = "+71234567890";
    private static final String CLIENT_EMAIL = "ivan@mail.com";
    private static final String PHOTOGRAPHER_FIRST_NAME = "Петр";
    private static final String PHOTOGRAPHER_LAST_NAME = "Сидоров";
    private static final double PHOTOGRAPHER_HOURLY_RATE = 1500.0;
    private static final double SESSION_TOTAL_PRICE = 8000.0;
    private static final double UPDATED_SESSION_TOTAL_PRICE = 9000.0;
    private static final String PORTRAIT_SERVICE_NAME = "Портретная съемка";
    private static final String WEDDING_SERVICE_NAME = "Свадебная съемка";

    @Mock
    private PhotoSessionRepository photoSessionRepository;

    @Mock
    private PhotoSessionMapper photoSessionMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PhotographerRepository photographerRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private PhotoSessionCache photoSessionCache;

    @InjectMocks
    private PhotoSessionService photoSessionService;

    private Client client;
    private Photographer photographer;
    private com.example.photostudio.model.PhotoService photoServiceModel;
    private PhotoSession photoSession;
    private PhotoSessionDto photoSessionDto;
    private PhotoSessionCreateDto createDto;
    private PhotoSessionUpdateDto updateDto;
    private Long existingId;
    private Long nonExistingId;
    private Long existingClientId;
    private Long existingPhotographerId;
    private Long existingServiceId;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 999L;
        existingClientId = 1L;
        existingPhotographerId = 1L;
        existingServiceId = 1L;

        client = Client.builder()
                .id(existingClientId)
                .firstName(CLIENT_FIRST_NAME)
                .lastName(CLIENT_LAST_NAME)
                .phone(CLIENT_PHONE)
                .email(CLIENT_EMAIL)
                .build();

        photographer = Photographer.builder()
                .id(existingPhotographerId)
                .firstName(PHOTOGRAPHER_FIRST_NAME)
                .lastName(PHOTOGRAPHER_LAST_NAME)
                .hourlyRate(PHOTOGRAPHER_HOURLY_RATE)
                .build();

        photoServiceModel = com.example.photostudio.model.PhotoService.builder()
                .id(existingServiceId)
                .serviceType(ServiceType.PORTRAIT)
                .build();

        photoSession = PhotoSession.builder()
                .id(existingId)
                .date(LocalDateTime.now().plusDays(1))
                .totalPrice(SESSION_TOTAL_PRICE)
                .client(client)
                .photographer(photographer)
                .service(photoServiceModel)
                .build();

        photoSessionDto = PhotoSessionDto.builder()
                .id(existingId)
                .date(photoSession.getDate())
                .totalPrice(SESSION_TOTAL_PRICE)
                .clientId(existingClientId)
                .clientName(CLIENT_FIRST_NAME)
                .clientLastName(CLIENT_LAST_NAME)
                .photographerId(existingPhotographerId)
                .photographerName(PHOTOGRAPHER_FIRST_NAME + " " + PHOTOGRAPHER_LAST_NAME)
                .serviceId(existingServiceId)
                .serviceName(PORTRAIT_SERVICE_NAME)
                .build();

        createDto = new PhotoSessionCreateDto();
        createDto.setDate(LocalDateTime.now().plusDays(1));
        createDto.setClientId(existingClientId);
        createDto.setPhotographerId(existingPhotographerId);
        createDto.setServiceId(existingServiceId);

        updateDto = new PhotoSessionUpdateDto();
        updateDto.setDate(LocalDateTime.now().plusDays(2));
        updateDto.setServiceId(2L);
    }

    @Test
    void getAllPhotoSessionsShouldReturnList() {
        List<PhotoSession> sessions = List.of(photoSession);

        when(photoSessionRepository.findAll()).thenReturn(sessions);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getAllPhotoSessions();

        assertThat(result).hasSize(1);
        verify(photoSessionRepository, times(1)).findAll();
    }

    @Test
    void getPhotoSessionByIdWhenExistsShouldReturn() {
        when(photoSessionRepository.findById(existingId))
                .thenReturn(Optional.of(photoSession));
        when(photoSessionMapper.toDto(photoSession))
                .thenReturn(photoSessionDto);

        PhotoSessionDto result = photoSessionService.getPhotoSessionById(existingId);

        assertThat(result)
                .isNotNull()
                .extracting(PhotoSessionDto::getId)
                .isEqualTo(existingId);
    }

    @Test
    void getPhotoSessionByIdWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        PhotoSessionDto result = photoSessionService.getPhotoSessionById(nonExistingId);

        assertThat(result).isNull();
    }

    @Test
    void getPhotoSessionsByClientIdShouldReturnList() {
        List<PhotoSession> sessions = List.of(photoSession);

        when(photoSessionRepository.findByClientId(existingClientId))
                .thenReturn(sessions);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getPhotoSessionsByClientId(existingClientId);

        assertThat(result).hasSize(1);
        verify(photoSessionRepository, times(1)).findByClientId(existingClientId);
    }

    @Test
    void createPhotoSessionShouldReturnCreated() {
        when(clientRepository.findById(existingClientId))
                .thenReturn(Optional.of(client));
        when(photographerRepository.findById(existingPhotographerId))
                .thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(existingServiceId))
                .thenReturn(Optional.of(photoServiceModel));
        when(photoSessionRepository.save(any(PhotoSession.class)))
                .thenReturn(photoSession);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);

        PhotoSessionDto result = photoSessionService.createPhotoSession(createDto);

        assertThat(result)
                .isNotNull()
                .extracting(PhotoSessionDto::getId)
                .isEqualTo(existingId);
        verify(photoSessionCache, times(1)).invalidateAll();
    }

    @Test
    void deletePhotoSessionWhenExistsShouldReturnTrue() {
        when(photoSessionRepository.findById(existingId))
                .thenReturn(Optional.of(photoSession));
        doNothing().when(photoSessionRepository).delete(any(PhotoSession.class));

        boolean result = photoSessionService.deletePhotoSession(existingId);

        assertThat(result).isTrue();
        verify(photoSessionRepository, times(1)).findById(existingId);
        verify(photoSessionRepository, times(1)).delete(any(PhotoSession.class));
    }

    @Test
    void deletePhotoSessionWhenNotExistsShouldReturnFalse() {
        when(photoSessionRepository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        boolean result = photoSessionService.deletePhotoSession(nonExistingId);

        assertThat(result).isFalse();
        verify(photoSessionRepository, never()).delete(any(PhotoSession.class));
    }

    @Test
    void updatePhotoSessionWhenExistsShouldReturnUpdated() {
        com.example.photostudio.model.PhotoService newService =
                com.example.photostudio.model.PhotoService.builder()
                        .id(2L)
                        .serviceType(ServiceType.WEDDING)
                        .build();

        PhotoSession updatedSession = PhotoSession.builder()
                .id(existingId)
                .date(updateDto.getDate())
                .totalPrice(UPDATED_SESSION_TOTAL_PRICE)
                .client(client)
                .photographer(photographer)
                .service(newService)
                .build();

        PhotoSessionDto updatedDto = PhotoSessionDto.builder()
                .id(existingId)
                .date(updateDto.getDate())
                .totalPrice(UPDATED_SESSION_TOTAL_PRICE)
                .clientId(existingClientId)
                .clientName(CLIENT_FIRST_NAME)
                .clientLastName(CLIENT_LAST_NAME)
                .photographerId(existingPhotographerId)
                .photographerName(PHOTOGRAPHER_FIRST_NAME + " " + PHOTOGRAPHER_LAST_NAME)
                .serviceId(2L)
                .serviceName(WEDDING_SERVICE_NAME)
                .build();

        when(photoSessionRepository.findById(existingId))
                .thenReturn(Optional.of(photoSession));
        when(serviceRepository.findById(2L))
                .thenReturn(Optional.of(newService));
        when(photoSessionRepository.save(any(PhotoSession.class)))
                .thenReturn(updatedSession);
        when(photoSessionMapper.toDto(updatedSession))
                .thenReturn(updatedDto);

        PhotoSessionDto result = photoSessionService.updatePhotoSession(existingId, updateDto);

        assertThat(result)
                .isNotNull()
                .extracting(PhotoSessionDto::getServiceId)
                .isEqualTo(2L);
        verify(photoSessionCache, times(1)).invalidateAll();
    }

    @Test
    void updatePhotoSessionWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        PhotoSessionDto result = photoSessionService.updatePhotoSession(
                nonExistingId, updateDto);

        assertThat(result).isNull();
        verify(photoSessionCache, never()).invalidateAll();
    }

    @Test
    void getSessionsWithFiltersShouldReturnFilteredList() {
        List<PhotoSession> sessions = List.of(photoSession);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_FIRST_NAME)
                .build();

        ArgumentCaptor<String> clientNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> photographerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);

        when(photoSessionRepository.findSessionsWithFiltersJpql(
                clientNameCaptor.capture(),
                photographerNameCaptor.capture(),
                phoneCaptor.capture()))
                .thenReturn(sessions);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersJpql(filter);

        assertThat(result).hasSize(1);

        assertThat(clientNameCaptor.getValue()).isEqualTo(CLIENT_FIRST_NAME);
        assertThat(photographerNameCaptor.getValue()).isNull();
        assertThat(phoneCaptor.getValue()).isNull();

        verify(photoSessionRepository, times(1))
                .findSessionsWithFiltersJpql(
                        clientNameCaptor.getValue(),
                        photographerNameCaptor.getValue(),
                        phoneCaptor.getValue());
    }

    @Test
    void getSessionsWithFiltersPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        List<PhotoSession> sessions = List.of(photoSession);
        Page<PhotoSession> page = new PageImpl<>(sessions, pageable, 1);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_FIRST_NAME)
                .page(0)
                .size(10)
                .sortBy("date")
                .sortDirection("DESC")
                .build();

        ArgumentCaptor<String> clientNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> photographerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                clientNameCaptor.capture(),
                photographerNameCaptor.capture(),
                phoneCaptor.capture(),
                pageableCaptor.capture()))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);

        Page<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        assertThat(clientNameCaptor.getValue()).isEqualTo(CLIENT_FIRST_NAME);
        assertThat(photographerNameCaptor.getValue()).isNull();
        assertThat(phoneCaptor.getValue()).isNull();
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);

        verify(photoSessionRepository, times(1))
                .findSessionsWithFiltersPaged(
                        clientNameCaptor.getValue(),
                        photographerNameCaptor.getValue(),
                        phoneCaptor.getValue(),
                        pageableCaptor.getValue());
    }

    @Test
    void getSessionsWithCacheWhenCacheHitShouldReturnFromCache() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_FIRST_NAME)
                .page(0)
                .size(10)
                .build();

        Page<PhotoSessionDto> cachedPage = new PageImpl<>(List.of(photoSessionDto));

        ArgumentCaptor<PhotoSessionQueryKey> keyCaptor = ArgumentCaptor.forClass(PhotoSessionQueryKey.class);
        when(photoSessionCache.get(keyCaptor.capture())).thenReturn(cachedPage);

        Page<PhotoSessionDto> result = photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        PhotoSessionQueryKey capturedKey = keyCaptor.getValue();
        assertThat(capturedKey.clientName()).isEqualTo(CLIENT_FIRST_NAME);
        assertThat(capturedKey.photographerName()).isNull();
        assertThat(capturedKey.phone()).isNull();
        assertThat(capturedKey.page()).isZero();
        assertThat(capturedKey.size()).isEqualTo(10);

        verify(photoSessionCache, times(1)).get(any(PhotoSessionQueryKey.class));
        verify(photoSessionRepository, never())
                .findSessionsWithFiltersPaged(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getSessionsWithCacheWhenCacheMissShouldQueryDatabaseAndCache() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_FIRST_NAME)
                .page(0)
                .size(10)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        List<PhotoSession> sessions = List.of(photoSession);
        Page<PhotoSession> page = new PageImpl<>(sessions, pageable, 1);

        ArgumentCaptor<PhotoSessionQueryKey> keyCaptor = ArgumentCaptor.forClass(PhotoSessionQueryKey.class);
        ArgumentCaptor<String> clientNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> photographerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(photoSessionCache.get(keyCaptor.capture())).thenReturn(null);
        when(photoSessionRepository.findSessionsWithFiltersPaged(
                clientNameCaptor.capture(),
                photographerNameCaptor.capture(),
                phoneCaptor.capture(),
                pageableCaptor.capture()))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any(PhotoSession.class)))
                .thenReturn(photoSessionDto);
        doNothing().when(photoSessionCache).put(any(PhotoSessionQueryKey.class), any());

        Page<PhotoSessionDto> result = photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();

        PhotoSessionQueryKey capturedKey = keyCaptor.getValue();
        assertThat(capturedKey.clientName()).isEqualTo(CLIENT_FIRST_NAME);
        assertThat(capturedKey.photographerName()).isNull();
        assertThat(capturedKey.phone()).isNull();
        assertThat(capturedKey.page()).isZero();
        assertThat(capturedKey.size()).isEqualTo(10);

        assertThat(clientNameCaptor.getValue()).isEqualTo(CLIENT_FIRST_NAME);
        assertThat(photographerNameCaptor.getValue()).isNull();
        assertThat(phoneCaptor.getValue()).isNull();
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);

        verify(photoSessionCache, times(1)).get(any(PhotoSessionQueryKey.class));
        verify(photoSessionRepository, times(1))
                .findSessionsWithFiltersPaged(
                        clientNameCaptor.getValue(),
                        photographerNameCaptor.getValue(),
                        phoneCaptor.getValue(),
                        pageableCaptor.getValue());
        verify(photoSessionCache, times(1)).put(any(PhotoSessionQueryKey.class), any());
    }

    @Test
    void getCacheSizeShouldReturnSize() {
        when(photoSessionCache.getCacheSize()).thenReturn(5);

        int result = photoSessionService.getCacheSize();

        assertThat(result).isEqualTo(5);
        verify(photoSessionCache, times(1)).getCacheSize();
    }
}