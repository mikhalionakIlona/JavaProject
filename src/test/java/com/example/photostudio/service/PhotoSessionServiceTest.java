package com.example.photostudio.service;

import com.example.photostudio.cache.PhotoSessionCache;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhotoSessionServiceTest {

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

        List<PhotoSessionDto> result = photoSessionService.getAllPhotoSessions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ID);
        verify(photoSessionRepository).findAll();
    }

    @Test
    void getPhotoSessionByIdWhenExistsShouldReturn() {
        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        when(photoSessionMapper.toDto(photoSession)).thenReturn(photoSessionDto);

        PhotoSessionDto result = photoSessionService.getPhotoSessionById(ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ID);
        verify(photoSessionRepository).findById(ID);
    }

    @Test
    void getPhotoSessionByIdWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        PhotoSessionDto result = photoSessionService.getPhotoSessionById(NON_EXISTENT_ID);

        assertThat(result).isNull();
    }

    @Test
    void getPhotoSessionsByClientIdShouldReturnList() {
        when(photoSessionRepository.findByClientId(ID)).thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getPhotoSessionsByClientId(ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(ID);
        verify(photoSessionRepository).findByClientId(ID);
    }

    @Test
    void createPhotoSessionShouldReturnCreated() {
        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(ID)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);
        when(photoSessionMapper.toDto(any(PhotoSession.class))).thenReturn(photoSessionDto);

        PhotoSessionDto result = photoSessionService.createPhotoSession(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ID);
        verify(photoSessionCache).invalidateAll();
    }

    @Test
    void deletePhotoSessionWhenExistsShouldReturnTrue() {
        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        doNothing().when(photoSessionRepository).delete(any());

        boolean result = photoSessionService.deletePhotoSession(ID);

        assertThat(result).isTrue();
        verify(photoSessionRepository).findById(ID);
        verify(photoSessionRepository).delete(any());
    }

    @Test
    void deletePhotoSessionWhenNotExistsShouldReturnFalse() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        boolean result = photoSessionService.deletePhotoSession(NON_EXISTENT_ID);

        assertThat(result).isFalse();
        verify(photoSessionRepository, never()).delete(any());
    }

    @Test
    void updatePhotoSessionWhenExistsShouldReturnUpdated() {
        com.example.photostudio.model.PhotoService newService = com.example.photostudio.model.PhotoService.builder()
                .id(2L)
                .serviceType(ServiceType.WEDDING)
                .build();

        PhotoSession updatedSession = PhotoSession.builder()
                .id(ID)
                .date(updateDto.getDate())
                .totalPrice(9000.0)
                .client(client)
                .photographer(photographer)
                .service(newService)
                .build();

        PhotoSessionDto updatedDto = PhotoSessionDto.builder()
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

        PhotoSessionDto result = photoSessionService.updatePhotoSession(ID, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getServiceId()).isEqualTo(2L);
        verify(photoSessionCache).invalidateAll();
    }

    @Test
    void updatePhotoSessionWhenNotExistsShouldReturnNull() {
        when(photoSessionRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        PhotoSessionDto result = photoSessionService.updatePhotoSession(NON_EXISTENT_ID, updateDto);

        assertThat(result).isNull();
        verify(photoSessionCache, never()).invalidateAll();
    }

    @Test
    void getSessionsWithFiltersShouldReturnFilteredList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersJpql(any(), any(), any()))
                .thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersJpql(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo(CLIENT_NAME);
        verify(photoSessionRepository).findSessionsWithFiltersJpql(CLIENT_NAME, null, null);
    }

    @Test
    void getSessionsWithFiltersNativeShouldReturnFilteredList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersNative(any(), any(), any()))
                .thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersNative(filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo(CLIENT_NAME);
        verify(photoSessionRepository).findSessionsWithFiltersNative(CLIENT_NAME, null, null);
    }

    @Test
    void getSessionsWithFiltersJpqlWhenNoResultsShouldReturnEmptyList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName("Несуществующий")
                .build();

        when(photoSessionRepository.findSessionsWithFiltersJpql(any(), any(), any()))
                .thenReturn(List.of());

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersJpql(filter);

        assertThat(result).isEmpty();
        verify(photoSessionRepository).findSessionsWithFiltersJpql("Несуществующий", null, null);
    }

    @Test
    void getSessionsWithFiltersNativeWhenNoResultsShouldReturnEmptyList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName("Несуществующий")
                .build();

        when(photoSessionRepository.findSessionsWithFiltersNative(any(), any(), any()))
                .thenReturn(List.of());

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersNative(filter);

        assertThat(result).isEmpty();
        verify(photoSessionRepository).findSessionsWithFiltersNative("Несуществующий", null, null);
    }

    @Test
    void getSessionsWithFiltersPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .page(0)
                .size(10)
                .sortBy("date")
                .sortDirection("DESC")
                .build();

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClientName()).isEqualTo(CLIENT_NAME);
    }

    @Test
    void getSessionsWithFiltersPagedWithSortingShouldReturnPage() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "totalPrice"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .page(1)
                .size(5)
                .sortBy("totalPrice")
                .sortDirection("ASC")
                .build();

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
    }

    @Test
    void getSessionsWithCacheWhenCacheHitShouldReturnFromCache() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .page(0)
                .size(10)
                .build();
        PageImpl<PhotoSessionDto> cachedPage = new PageImpl<>(List.of(photoSessionDto));

        when(photoSessionCache.get(any())).thenReturn(cachedPage);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClientName()).isEqualTo(CLIENT_NAME);
        verify(photoSessionCache).get(any());
        verify(photoSessionRepository, never())
                .findSessionsWithFiltersPaged(any(), any(), any(), any());
    }

    @Test
    void getSessionsWithCacheWhenCacheMissShouldQueryDatabaseAndCache() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .page(0)
                .size(10)
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);

        when(photoSessionCache.get(any())).thenReturn(null);
        when(photoSessionRepository.findSessionsWithFiltersPaged(
                any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);
        doNothing().when(photoSessionCache).put(any(), any());

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(photoSessionCache).get(any());
        verify(photoSessionRepository).findSessionsWithFiltersPaged(
                any(), any(), any(), any(Pageable.class));
        verify(photoSessionCache).put(any(), any());
    }

    @Test
    void getCacheSizeShouldReturnSize() {
        when(photoSessionCache.getCacheSize()).thenReturn(5);

        int result = photoSessionService.getCacheSize();

        assertThat(result).isEqualTo(5);
        verify(photoSessionCache).getCacheSize();
    }

    @Test
    void demonstrateNPlus1ProblemShouldExecuteWithoutException() {
        photoSessionService.demonstrateNPlus1Problem();
        verify(photoSessionRepository).findAllWithoutFetch();
    }

    @Test
    void demonstrateEntityGraphSolutionShouldExecuteWithoutException() {
        photoSessionService.demonstrateEntityGraphSolution();
        verify(photoSessionRepository).findAllWithEntityGraph();
    }

    @Test
    void createWithRelatedWithTransactionShouldCreate() {
        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(ID)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);

        photoSessionService.createWithRelatedWithTransaction(createDto);

        verify(photoSessionRepository, times(1)).save(any(PhotoSession.class));
    }

    @Test
    void createWithRelatedWithoutTransactionShouldCreate() {
        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(ID)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);

        photoSessionService.createWithRelatedWithoutTransaction(createDto);

        verify(photoSessionRepository, times(1)).save(any(PhotoSession.class));
    }

    @Test
    void createWithRelatedWithTransactionWhenServiceId3ShouldThrow() {
        createDto.setServiceId(3L);

        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(3L)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);

        assertThatThrownBy(() -> photoSessionService.createWithRelatedWithTransaction(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Демонстрационная ошибка");

        verify(photoSessionRepository, times(1)).save(any(PhotoSession.class));
    }

    @Test
    void createWithRelatedWithoutTransactionWhenServiceId3ShouldThrow() {
        createDto.setServiceId(3L);

        when(clientRepository.findById(ID)).thenReturn(Optional.of(client));
        when(photographerRepository.findById(ID)).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(3L)).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);

        assertThatThrownBy(() -> photoSessionService.createWithRelatedWithoutTransaction(createDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Демонстрационная ошибка");

        verify(photoSessionRepository, times(1)).save(any(PhotoSession.class));
    }

    @Test
    void getSessionsWithFiltersPagedWithNullFiltersShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(null)
                .photographerName(null)
                .phone(null)
                .page(0)
                .size(10)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getSessionsWithFiltersNativeWithNullFiltersShouldReturnList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(null)
                .photographerName(null)
                .phone(null)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersNative(isNull(), isNull(), isNull()))
                .thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersNative(filter);

        assertThat(result).hasSize(1);
        verify(photoSessionRepository).findSessionsWithFiltersNative(null, null, null);
    }

    @Test
    void getSessionsWithFiltersJpqlWithNullFiltersShouldReturnList() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(null)
                .photographerName(null)
                .phone(null)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersJpql(isNull(), isNull(), isNull()))
                .thenReturn(List.of(photoSession));
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        List<PhotoSessionDto> result = photoSessionService.getSessionsWithFiltersJpql(filter);

        assertThat(result).hasSize(1);
        verify(photoSessionRepository).findSessionsWithFiltersJpql(null, null, null);
    }

    @Test
    void getSessionsWithFiltersPagedWithAllFiltersShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(CLIENT_NAME)
                .photographerName(PHOTOGRAPHER_NAME)
                .phone("+375")
                .page(0)
                .size(10)
                .build();

        when(photoSessionRepository.findSessionsWithFiltersPaged(
                eq(CLIENT_NAME), eq(PHOTOGRAPHER_NAME), eq("+375"), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithFiltersPaged(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getSessionsWithCacheWhenCacheKeyIsNullShouldQueryDatabase() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(null)
                .page(0)
                .size(10)
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        PageImpl<PhotoSession> page = new PageImpl<>(List.of(photoSession), pageable, 1);

        when(photoSessionCache.get(any())).thenReturn(null);
        when(photoSessionRepository.findSessionsWithFiltersPaged(
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(photoSessionMapper.toDto(any())).thenReturn(photoSessionDto);
        doNothing().when(photoSessionCache).put(any(), any());

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(photoSessionCache).put(any(), any());
    }

    @Test
    void getSessionsWithCacheWhenCacheKeyIsNullAndCacheHitShouldReturnFromCache() {
        PhotoSessionFilterDto filter = PhotoSessionFilterDto.builder()
                .clientName(null)
                .page(0)
                .size(10)
                .build();
        PageImpl<PhotoSessionDto> cachedPage = new PageImpl<>(List.of(photoSessionDto));

        when(photoSessionCache.get(any())).thenReturn(cachedPage);

        org.springframework.data.domain.Page<PhotoSessionDto> result =
                photoSessionService.getSessionsWithCache(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(photoSessionRepository, never())
                .findSessionsWithFiltersPaged(any(), any(), any(), any());
    }

    @Test
    void createPhotoSessionWithNullFieldsShouldHandleGracefully() {
        PhotoSessionCreateDto nullCreateDto = new PhotoSessionCreateDto();
        nullCreateDto.setDate(null);
        nullCreateDto.setClientId(null);
        nullCreateDto.setPhotographerId(null);
        nullCreateDto.setServiceId(null);

        when(clientRepository.findById(any())).thenReturn(Optional.of(client));
        when(photographerRepository.findById(any())).thenReturn(Optional.of(photographer));
        when(serviceRepository.findById(any())).thenReturn(Optional.of(photoService));
        when(photoSessionRepository.save(any(PhotoSession.class))).thenReturn(photoSession);
        when(photoSessionMapper.toDto(any(PhotoSession.class))).thenReturn(photoSessionDto);

        PhotoSessionDto result = photoSessionService.createPhotoSession(nullCreateDto);

        assertThat(result).isNotNull();
        verify(photoSessionRepository, times(1)).save(any(PhotoSession.class));
    }

    @Test
    void updatePhotoSessionWithNullServiceIdShouldOnlyUpdateDate() {
        PhotoSessionUpdateDto updateOnlyDate = new PhotoSessionUpdateDto();
        updateOnlyDate.setDate(LocalDateTime.now().plusDays(2));
        updateOnlyDate.setServiceId(null);

        PhotoSession updatedSession = PhotoSession.builder()
                .id(ID)
                .date(updateOnlyDate.getDate())
                .totalPrice(8000.0)
                .client(client)
                .photographer(photographer)
                .service(photoService)
                .build();

        PhotoSessionDto updatedDto = PhotoSessionDto.builder()
                .id(ID)
                .date(updateOnlyDate.getDate())
                .totalPrice(8000.0)
                .serviceId(ID)
                .serviceName("Портретная съемка")
                .build();

        when(photoSessionRepository.findById(ID)).thenReturn(Optional.of(photoSession));
        when(photoSessionRepository.save(any())).thenReturn(updatedSession);
        when(photoSessionMapper.toDto(updatedSession)).thenReturn(updatedDto);

        PhotoSessionDto result = photoSessionService.updatePhotoSession(ID, updateOnlyDate);

        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(updateOnlyDate.getDate());
        assertThat(result.getServiceId()).isEqualTo(ID);
        verify(photoSessionCache).invalidateAll();
    }
}