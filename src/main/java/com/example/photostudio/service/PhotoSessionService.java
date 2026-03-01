package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository; // Оставляем ServiceRepository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository; // Оставляем имя serviceRepository

    // GET с @RequestParam
    public List<PhotoSessionDto> getAllPhotoSessions(String clientName) {
        if (clientName != null && !clientName.isEmpty()) {
            return repository.findByClientNameIgnoreCase(clientName)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
        }
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // GET с @PathVariable
    public PhotoSessionDto getPhotoSessionById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    // CRUD operations
    public PhotoSessionDto createPhotoSession(PhotoSessionDto dto) {
        PhotoSession session = mapper.toEntity(dto);
        setRelatedEntities(session, dto);
        PhotoSession saved = repository.save(session);
        return mapper.toDto(saved);
    }

    public PhotoSessionDto updatePhotoSession(Long id, PhotoSessionDto dto) {
        return repository.findById(id)
                .map(session -> {
                    mapper.updateEntity(session, dto);
                    setRelatedEntities(session, dto);
                    return mapper.toDto(repository.save(session));
                })
                .orElse(null);
    }

    public boolean deletePhotoSession(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // Демонстрация N+1 проблемы
    public List<PhotoSessionDto> getAllWithNPlus1() {
        return repository.findAllWithoutFetch()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // Решение через JOIN FETCH
    public List<PhotoSessionDto> getAllWithJoinFetch() {
        return repository.findAllWithJoinFetch()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // Решение через EntityGraph
    public List<PhotoSessionDto> getAllWithEntityGraph() {
        return repository.findAllWithEntityGraph()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // Сохранение нескольких связанных сущностей БЕЗ @Transactional
    public PhotoSessionDto createWithRelatedNoTransaction(PhotoSessionDto dto, Long clientId,
                                                          Long photographerId, Long serviceId) {
        PhotoSession session = mapper.toEntity(dto);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> new NoSuchElementException("Photographer not found with id: " + photographerId));
        PhotoService photoService = serviceRepository.findById(serviceId) // Используем serviceRepository.findById
                .orElseThrow(() -> new NoSuchElementException("Service not found with id: " + serviceId));

        session.setClient(client);
        session.setPhotographerEntity(photographer);
        session.setService(photoService);

        PhotoSession saved = repository.save(session);

        // Здесь может быть ошибка, данные сохранятся частично
        if (saved.getPrice() > 1000) {
            throw new IllegalStateException("Price too high: " + saved.getPrice());
        }

        return mapper.toDto(saved);
    }

    // Сохранение нескольких связанных сущностей С @Transactional
    @Transactional
    public PhotoSessionDto createWithRelatedWithTransaction(PhotoSessionDto dto, Long clientId,
                                                            Long photographerId, Long serviceId) {
        PhotoSession session = mapper.toEntity(dto);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> new NoSuchElementException("Photographer not found with id: " + photographerId));
        PhotoService photoService = serviceRepository.findById(serviceId) // Используем serviceRepository.findById
                .orElseThrow(() -> new NoSuchElementException("Service not found with id: " + serviceId));

        session.setClient(client);
        session.setPhotographerEntity(photographer);
        session.setService(photoService);

        PhotoSession saved = repository.save(session);

        // При ошибке всё откатится
        if (saved.getPrice() > 1000) {
            throw new IllegalStateException("Price too high: " + saved.getPrice());
        }

        return mapper.toDto(saved);
    }

    private void setRelatedEntities(PhotoSession session, PhotoSessionDto dto) {
        if (dto.getClientId() != null) {
            session.setClient(clientRepository.getReferenceById(dto.getClientId()));
        }
        if (dto.getPhotographerId() != null) {
            session.setPhotographerEntity(photographerRepository.getReferenceById(dto.getPhotographerId()));
        }
        if (dto.getServiceId() != null) {
            session.setService(serviceRepository.getReferenceById(dto.getServiceId())); // Используем serviceRepository
        }
    }
}