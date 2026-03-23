package com.example.photostudio.service;

import com.example.photostudio.dto.photographer.PhotographerCreateDto;
import com.example.photostudio.dto.photographer.PhotographerDto;
import com.example.photostudio.dto.photographer.PhotographerUpdateDto;
import com.example.photostudio.mapper.PhotographerMapper;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.repository.PhotographerRepository;
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
class PhotographerServiceTest {

    @Mock
    private PhotographerRepository photographerRepository;

    @Mock
    private PhotographerMapper photographerMapper;

    @InjectMocks
    private PhotographerService photographerService;

    private PhotographerCreateDto createDto;
    private Photographer photographer;
    private PhotographerDto photographerDto;
    private PhotographerUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        createDto = new PhotographerCreateDto();
        createDto.setFirstName("Петр");
        createDto.setLastName("Сидоров");
        createDto.setPatronymic("Иванович");
        createDto.setPhone("+375298587885");
        createDto.setHourlyRate(1500.0);

        updateDto = new PhotographerUpdateDto();
        updateDto.setFirstName("Алексей");
        updateDto.setHourlyRate(2000.0);

        photographer = Photographer.builder()
                .id(1L)
                .firstName("Петр")
                .lastName("Сидоров")
                .patronymic("Иванович")
                .phone("+375298587885")
                .hourlyRate(1500.0)
                .build();

        photographerDto = PhotographerDto.builder()
                .id(1L)
                .firstName("Петр")
                .lastName("Сидоров")
                .patronymic("Иванович")
                .phone("+375298587885")
                .hourlyRate(1500.0)
                .build();
    }

    @Test
    void getAllPhotographersShouldReturnList() {
        when(photographerRepository.findAll()).thenReturn(List.of(photographer));
        when(photographerMapper.toDto(any(Photographer.class))).thenReturn(photographerDto);

        List<PhotographerDto> result = photographerService.getAllPhotographers();

        assertThat(result).hasSize(1);
        verify(photographerRepository, times(1)).findAll();
    }

    @Test
    void getPhotographerByIdWhenExistsShouldReturn() {
        when(photographerRepository.findById(1L)).thenReturn(Optional.of(photographer));
        when(photographerMapper.toDto(photographer)).thenReturn(photographerDto);

        PhotographerDto result = photographerService.getPhotographerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getPhotographerByIdWhenNotExistsShouldReturnNull() {
        when(photographerRepository.findById(999L)).thenReturn(Optional.empty());

        PhotographerDto result = photographerService.getPhotographerById(999L);

        assertThat(result).isNull();
    }

    @Test
    void createPhotographerShouldReturnCreated() {
        when(photographerRepository.save(any(Photographer.class))).thenReturn(photographer);
        when(photographerMapper.toDto(any(Photographer.class))).thenReturn(photographerDto);

        PhotographerDto result = photographerService.createPhotographer(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Петр");
    }

    @Test
    void updatePhotographerWhenExistsShouldReturnUpdated() {
        Photographer updatedPhotographer = Photographer.builder()
                .id(1L)
                .firstName("Алексей")
                .lastName("Сидоров")
                .patronymic("Иванович")
                .phone("+375298587885")
                .hourlyRate(2000.0)
                .build();

        PhotographerDto updatedDto = PhotographerDto.builder()
                .id(1L)
                .firstName("Алексей")
                .lastName("Сидоров")
                .patronymic("Иванович")
                .phone("+375298587885")
                .hourlyRate(2000.0)
                .build();

        when(photographerRepository.findById(1L)).thenReturn(Optional.of(photographer));
        when(photographerRepository.save(any(Photographer.class))).thenReturn(updatedPhotographer);
        when(photographerMapper.toDto(updatedPhotographer)).thenReturn(updatedDto);

        PhotographerDto result = photographerService.updatePhotographer(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Алексей");
        assertThat(result.getHourlyRate()).isEqualTo(2000.0);
    }

    @Test
    void updatePhotographerWhenNotExistsShouldReturnNull() {
        when(photographerRepository.findById(999L)).thenReturn(Optional.empty());

        PhotographerDto result = photographerService.updatePhotographer(999L, updateDto);

        assertThat(result).isNull();
    }

    @Test
    void deletePhotographerWhenExistsShouldReturnTrue() {
        when(photographerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(photographerRepository).deleteById(1L);

        boolean result = photographerService.deletePhotographer(1L);

        assertThat(result).isTrue();
    }

    @Test
    void deletePhotographerWhenNotExistsShouldReturnFalse() {
        when(photographerRepository.existsById(999L)).thenReturn(false);

        boolean result = photographerService.deletePhotographer(999L);

        assertThat(result).isFalse();
    }
}