package com.example.photostudio.service;

import com.example.photostudio.dto.photo.PhotoDto;
import com.example.photostudio.mapper.PhotoMapper;
import com.example.photostudio.model.Photo;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.PhotoRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    private static final String FILE_NAME = "test-photo.jpg";
    private static final String FILE_PATH = "/uploads/test-photo.jpg";
    private static final String NEW_FILE_NAME = "new-photo.jpg";
    private static final String NEW_FILE_PATH = "/uploads/new-photo.jpg";

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private PhotoSessionRepository photoSessionRepository;

    @InjectMocks
    private PhotoService photoService;

    private Photo photo;
    private PhotoDto photoDto;
    private PhotoSession photoSession;
    private Long existingId;
    private Long nonExistingId;
    private Long existingSessionId;
    private Long nonExistingSessionId;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 999L;
        existingSessionId = 1L;
        nonExistingSessionId = 999L;

        photoSession = PhotoSession.builder()
                .id(existingSessionId)
                .date(LocalDateTime.now())
                .totalPrice(10000.0)
                .build();

        photo = Photo.builder()
                .id(existingId)
                .fileName(FILE_NAME)
                .filePath(FILE_PATH)
                .uploadDate(LocalDateTime.now())
                .photoSession(photoSession)
                .build();

        photoDto = PhotoDto.builder()
                .id(existingId)
                .fileName(FILE_NAME)
                .filePath(FILE_PATH)
                .uploadDate(photo.getUploadDate())
                .sessionId(existingSessionId)
                .build();
    }

    @Test
    void getAllPhotosShouldReturnListOfPhotos() {
        List<Photo> photos = List.of(photo);

        when(photoRepository.findAll()).thenReturn(photos);
        when(photoMapper.toDto(any(Photo.class))).thenReturn(photoDto);

        List<PhotoDto> result = photoService.getAllPhotos();

        assertThat(result)
                .isNotNull()
                .hasSize(1);

        assertThat(result.get(0))
                .isNotNull()
                .extracting(PhotoDto::getId, PhotoDto::getFileName)
                .containsExactly(existingId, FILE_NAME);

        verify(photoRepository, times(1)).findAll();
    }

    @Test
    void getAllPhotosWhenNoPhotosShouldReturnEmptyList() {
        when(photoRepository.findAll()).thenReturn(List.of());

        List<PhotoDto> result = photoService.getAllPhotos();

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(photoRepository, times(1)).findAll();
    }

    @Test
    void getPhotoByIdWhenPhotoExistsShouldReturnPhoto() {
        when(photoRepository.findById(existingId)).thenReturn(Optional.of(photo));
        when(photoMapper.toDto(photo)).thenReturn(photoDto);

        PhotoDto result = photoService.getPhotoById(existingId);

        assertThat(result)
                .isNotNull()
                .extracting(PhotoDto::getId, PhotoDto::getFileName)
                .containsExactly(existingId, FILE_NAME);

        verify(photoRepository, times(1)).findById(existingId);
    }

    @Test
    void getPhotoByIdWhenPhotoNotExistsShouldReturnNull() {
        when(photoRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        PhotoDto result = photoService.getPhotoById(nonExistingId);

        assertThat(result).isNull();
        verify(photoRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void getPhotosBySessionIdWhenSessionHasPhotosShouldReturnPhotos() {
        List<Photo> photos = List.of(photo);

        when(photoRepository.findByPhotoSessionId(existingSessionId)).thenReturn(photos);
        when(photoMapper.toDto(any(Photo.class))).thenReturn(photoDto);

        List<PhotoDto> result = photoService.getPhotosBySessionId(existingSessionId);

        assertThat(result)
                .isNotNull()
                .hasSize(1);

        verify(photoRepository, times(1)).findByPhotoSessionId(existingSessionId);
    }

    @Test
    void getPhotosBySessionIdWhenSessionHasNoPhotosShouldReturnEmptyList() {
        when(photoRepository.findByPhotoSessionId(existingSessionId)).thenReturn(List.of());

        List<PhotoDto> result = photoService.getPhotosBySessionId(existingSessionId);

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(photoRepository, times(1)).findByPhotoSessionId(existingSessionId);
    }

    @Test
    void createPhotoWhenSessionExistsShouldReturnCreatedPhoto() {
        when(photoSessionRepository.findById(existingSessionId)).thenReturn(Optional.of(photoSession));
        when(photoRepository.save(any(Photo.class))).thenReturn(photo);
        when(photoMapper.toDto(any(Photo.class))).thenReturn(photoDto);

        PhotoDto result = photoService.createPhoto(NEW_FILE_NAME, NEW_FILE_PATH, existingSessionId);

        assertThat(result)
                .isNotNull()
                .extracting(PhotoDto::getFileName)
                .isEqualTo(FILE_NAME);

        verify(photoSessionRepository, times(1)).findById(existingSessionId);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void createPhotoWhenSessionNotExistsShouldThrowNoSuchElementException() {
        when(photoSessionRepository.findById(nonExistingSessionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.createPhoto(NEW_FILE_NAME, NEW_FILE_PATH, nonExistingSessionId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("PhotoSession not found");

        verify(photoSessionRepository, times(1)).findById(nonExistingSessionId);
        verify(photoRepository, never()).save(any(Photo.class));
    }

    @Test
    void deletePhotoWhenPhotoExistsShouldReturnTrue() {
        when(photoRepository.existsById(existingId)).thenReturn(true);
        doNothing().when(photoRepository).deleteById(existingId);

        boolean result = photoService.deletePhoto(existingId);

        assertThat(result).isTrue();
        verify(photoRepository, times(1)).existsById(existingId);
        verify(photoRepository, times(1)).deleteById(existingId);
    }

    @Test
    void deletePhotoWhenPhotoNotExistsShouldReturnFalse() {
        when(photoRepository.existsById(nonExistingId)).thenReturn(false);

        boolean result = photoService.deletePhoto(nonExistingId);

        assertThat(result).isFalse();
        verify(photoRepository, times(1)).existsById(nonExistingId);
        verify(photoRepository, never()).deleteById(anyLong());
    }
}