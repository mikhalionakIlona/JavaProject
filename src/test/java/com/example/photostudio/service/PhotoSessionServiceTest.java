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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhotoSessionServiceTest {

    @Mock private PhotoSessionRepository photoSessionRepository;
    @Mock private PhotoSessionMapper photoSessionMapper;
    @Mock private ClientRepository clientRepository;
    @Mock private PhotographerRepository photographerRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private PhotoSessionCache photoSessionCache;
    @InjectMocks private PhotoSessionService photoSessionService;

    private static final String CLIENT_NAME = "Иван";
    private static final String PHOTOGRAPHER_NAME = "Петр Сидоров";
    private static final long ID = 1L;
    private static final long NON_EXISTENT_ID = 999L;

    private Client client;
    private Photographer photographer;
    private com.example.photostudio.model.PhotoService photoService;
    private PhotoSession photoSession;
    private PhotoSessionDto photoSessionDto;
    private PhotoSessionCreateDto createDto;
    private PhotoSessionUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(ID)
                .firstName(CLIENT_NAME)
                .lastName("Петров")
                .phone("+375298587885")
                .email("ivan@mail.com")
                .build();

        photographer = Photographer.builder()
                .id(ID)
                .firstName("Петр")
                .lastName("Сидоров")
                .hourlyRate(1500.0)
                .build();

        photoService = com.example.photostudio.model.PhotoService.builder()
                .id(ID)
                .serviceType(ServiceType.PORTRAIT)
                .build();

        photoSession = PhotoSession.builder()
                .id(ID)
                .date(LocalDateTime.now().plusDays(1))
                .totalPrice(8000.0)
                .client(client)
                .photographer(photographer)
                .service(photoService)
                .build();

        photoSessionDto = PhotoSessionDto.builder()
                .id(ID)
                .date(photoSession.getDate())
                .totalPrice(8000.0)
                .clientId(ID)
                .clientName(CLIENT_NAME)
                .clientLastName("Петров")
                .photographerId(ID)
                .photographerName(PHOTOGRAPHER_NAME)
                .serviceId(ID)
                .serviceName("Портретная съемка")
                .build();

        createDto = new PhotoSessionCreateDto();
        createDto.setDate(LocalDateTime.now().plusDays(1));
        createDto.setClientId(ID);
        createDto.setPhotographerId(ID);
        createDto.setServiceId(ID);

        updateDto = new PhotoSessionUpdateDto();
        updateDto.setDate(LocalDateTime.now().plusDays(2));
        updateDto.setServiceId(2L);
    }

    @Test
    void getAllPhotoSessionsShouldReturnList() {
        when(photoSessionRepository.findAll()).thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        var result = photoSessionService.getAllPhotoSessions();

        assertThat(result).hasSize(1);
        verify(photoSessionRepository).findAll();
    }

    @Test
    void getPhotoSessionByIdWhenExistsShouldReturn() {
        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        when(photoSessionMapper.toDto(photoSession)).thenReturn(photoSessionDto);

        var result = photoSessionService.getPhotoSessionById(ID);

        assertThat(result).isNotNull().extracting(PhotoSessionDto::getId).isEqualTo(ID);
        verify(photoSessionRepository).findById(ID);
    }

    @Test
    void getPhotoSessionByIdWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThat(photoSessionService.getPhotoSessionById(NON_EXISTENT_ID)).isNull();
    }

    @Test
    void getPhotoSessionsByClientIdShouldReturnList() {
        when(photoSessionRepository.findByClientId(ID)).thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        var result = photoSessionService.getPhotoSessionsByClientId(ID);

        assertThat(result).hasSize(1);
        verify(photoSessionRepository).findByClientId(ID);
    }

    @Test
    void createPhotoSessionShouldReturnCreated() {
        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(ID)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);
        when(photoSessionMapper.toDto(any(PhotoSession.class))).thenReturn(photoSessionDto);

        var result = photoSessionService.createPhotoSession(createDto);

        assertThat(result).isNotNull().extracting(PhotoSessionDto::getId).isEqualTo(ID);
        verify(photoSessionCache).invalidateAll();
    }

    @Test
    void deletePhotoSessionWhenExistsShouldReturnTrue() {
        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        doNothing().when(photoSessionRepository).delete(any());

        assertThat(photoSessionService.deletePhotoSession(ID)).isTrue();
        verify(photoSessionRepository).findById(ID);
        verify(photoSessionRepository).delete(any());
    }

    @Test
    void deletePhotoSessionWhenNotExistsShouldReturnFalse() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThat(photoSessionService.deletePhotoSession(NON_EXISTENT_ID)).isFalse();
        verify(photoSessionRepository, never()).delete(any());
    }

    @Test
    void updatePhotoSessionWhenExistsShouldReturnUpdated() {
        var newService = com.example.photostudio.model.PhotoService.builder()
                .id(2L)
                .serviceType(ServiceType.WEDDING)
                .build();

        var updatedSession = PhotoSession.builder()
                .id(ID)
                .date(updateDto.getDate())
                .totalPrice(9000.0)
                .client(client)
                .photographer(photographer)
                .service(newService)
                .build();

        var updatedDto = PhotoSessionDto.builder()
                .id(ID)
                .date(updateDto.getDate())
                .totalPrice(9000.0)
                .serviceId(2L)
                .serviceName("Свадебная съемка")
                .build();

        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        when(serviceRepository.findById(2L)).thenReturn(Optional.of(newService));
        when(photoSessionRepository.save(any())).thenReturn(updatedSession);
        when(photoSessionMapper.toDto(updatedSession)).thenReturn(updatedDto);

        var result = photoSessionService.updatePhotoSession(ID, updateDto);

        assertThat(result).isNotNull().extracting(PhotoSessionDto::getServiceId).isEqualTo(2L);
        verify(photoSessionCache).invalidateAll();
    }

    @Test
    void updatePhotoSessionWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        assertThat(photoSessionService.updatePhotoSession(NON_EXISTENT_ID, updateDto)).isNull();
        verify(photoSessionCache, never()).invalidateAll();
    }

    @Test
    void getSessionsWithFiltersShouldReturnFilteredList() {
        var filter = PhotoSessionFilterDto.builder().clientName(CLIENT_NAME).build();

        when(photoSessionRepository.findSessionsWithFiltersJpql(any(), any(), any())).thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        var result = photoSessionService.getSessionsWithFiltersJpql(filter);

        assertThat(result).hasSize(1);
        verify(photoSessionRepository).findSessionsWithFiltersJpql(CLIENT_NAME, null, null);
    }

    @Test
    void getSessionsWithFiltersPagedShouldReturnPage() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        var page = new PageImpl<>(List.of(photoSession), pageable, 1);
        var filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .page(0)
                .size(10)
                .sortBy("date")
                .sortDirection("DESC")
                .build();

        var clientCaptor = ArgumentCaptor.forClass(String.class);
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                clientCaptor.capture(), any(), any(), pageableCaptor.capture()))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        var result = photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(clientCaptor.getValue()).isEqualTo(CLIENT_NAME);
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getSessionsWithCacheWhenCacheHitShouldReturnFromCache() {
        var filter = PhotoSessionFilterDto.builder().clientName(CLIENT_NAME).page(0).size(10).build();
        var cachedPage = new PageImpl<>(List.of(photoSessionDto));

        when(photoSessionCache.get(any())).thenReturn(cachedPage);

        var result = photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(photoSessionCache).get(any());
        verify(photoSessionRepository, never()).findSessionsWithFiltersPaged(any(), any(), any(), any());
    }

    @Test
    void getSessionsWithCacheWhenCacheMissShouldQueryDatabaseAndCache() {
        var filter = PhotoSessionFilterDto.builder().clientName(CLIENT_NAME).page(0).size(10).build();
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        var page = new PageImpl<>(List.of(photoSession), pageable, 1);

        var keyCaptor = ArgumentCaptor.forClass(PhotoSessionQueryKey.class);
        var clientCaptor = ArgumentCaptor.forClass(String.class);
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(photoSessionCache.get(keyCaptor.capture())).thenReturn(null);
        when(photoSessionRepository.findSessionsWithFiltersPaged(
                clientCaptor.capture(), any(), any(), pageableCaptor.capture()))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);
        doNothing().when(photoSessionCache).put(any(), any());

        var result = photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(keyCaptor.getValue().clientName()).isEqualTo(CLIENT_NAME);
        assertThat(clientCaptor.getValue()).isEqualTo(CLIENT_NAME);
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        verify(photoSessionCache).get(any());
        verify(photoSessionRepository).findSessionsWithFiltersPaged(any(), any(), any(), any());
        verify(photoSessionCache).put(any(), any());
    }

    @Test
    void getCacheSizeShouldReturnSize() {
        when(photoSessionCache.getCacheSize()).thenReturn(5);
        assertThat(photoSessionService.getCacheSize()).isEqualTo(5);
        verify(photoSessionCache).getCacheSize();
    }
}