package com.example.photostudio.service;

import com.example.photostudio.dto.service.ServiceCreateDto;
import com.example.photostudio.dto.service.ServiceDto;
import com.example.photostudio.dto.service.ServiceUpdateDto;
import com.example.photostudio.mapper.ServiceMapper;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.model.ServiceType;
import com.example.photostudio.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private PhotoServiceService photoServiceService;

    private ServiceCreateDto createDto;
    private PhotoService service;
    private ServiceDto serviceDto;
    private ServiceUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        createDto = new ServiceCreateDto();
        createDto.setServiceType(ServiceType.PORTRAIT);

        updateDto = new ServiceUpdateDto();
        updateDto.setServiceType(ServiceType.WEDDING);

        service = PhotoService.builder()
                .id(1L)
                .serviceType(ServiceType.PORTRAIT)
                .build();

        serviceDto = ServiceDto.builder()
                .id(1L)
                .serviceType(ServiceType.PORTRAIT)
                .name("Портретная съемка")
                .build();
    }

    @Test
    void getAllServicesShouldReturnList() {
        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(serviceMapper.toDto(any(PhotoService.class))).thenReturn(serviceDto);

        List<ServiceDto> result = photoServiceService.getAllServices();

        assertThat(result).hasSize(1);
        verify(serviceRepository, times(1)).findAll();
    }

    @Test
    void getServiceByIdWhenExistsShouldReturn() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        ServiceDto result = photoServiceService.getServiceById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getServiceByIdWhenNotExistsShouldReturnNull() {
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        ServiceDto result = photoServiceService.getServiceById(999L);

        assertThat(result).isNull();
    }

    @Test
    void createServiceShouldReturnCreated() {
        when(serviceMapper.toEntity(any(ServiceType.class))).thenReturn(service);
        when(serviceRepository.save(any(PhotoService.class))).thenReturn(service);
        when(serviceMapper.toDto(any(PhotoService.class))).thenReturn(serviceDto);

        ServiceDto result = photoServiceService.createService(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.PORTRAIT);
    }

    @Test
    void updateServiceWhenExistsShouldReturnUpdated() {
        PhotoService updatedService = PhotoService.builder()
                .id(1L)
                .serviceType(ServiceType.WEDDING)
                .build();

        ServiceDto updatedDto = ServiceDto.builder()
                .id(1L)
                .serviceType(ServiceType.WEDDING)
                .name("Свадебная съемка")
                .build();

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(PhotoService.class))).thenReturn(updatedService);
        when(serviceMapper.toDto(updatedService)).thenReturn(updatedDto);

        ServiceDto result = photoServiceService.updateService(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.WEDDING);
    }

    @Test
    void updateServiceWhenNotExistsShouldReturnNull() {
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        ServiceDto result = photoServiceService.updateService(999L, updateDto);

        assertThat(result).isNull();
    }

    @Test
    void deleteServiceWhenExistsShouldReturnTrue() {
        when(serviceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(serviceRepository).deleteById(1L);

        boolean result = photoServiceService.deleteService(1L);

        assertThat(result).isTrue();
    }

    @Test
    void deleteServiceWhenNotExistsShouldReturnFalse() {
        when(serviceRepository.existsById(999L)).thenReturn(false);

        boolean result = photoServiceService.deleteService(999L);

        assertThat(result).isFalse();
    }
}