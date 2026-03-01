package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository;

    // Внедрение самого себя для корректной работы @Transactional
    private final PhotoSessionService self;

    // GET с @RequestParam
    public List<PhotoSessionDto> getAllPhotoSessions(String clientName) {
        List<PhotoSession> sessions;

        if (clientName != null && !clientName.isEmpty()) {
            sessions = repository.findByClientNameIgnoreCase(clientName);
        } else {
            sessions = repository.findAll();
        }

        return mapToDtoList(sessions);
    }

    // GET с @PathVariable
    public PhotoSessionDto getPhotoSessionById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    // CRUD operations
    public PhotoSessionDto createPhotoSession(PhotoSessionDto dto) {
        return savePhotoSession(new PhotoSession(), dto);
    }

    public PhotoSessionDto updatePhotoSession(Long id, PhotoSessionDto dto) {
        return repository.findById(id)
                .map(session -> savePhotoSession(session, dto))
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
        return mapToDtoList(repository.findAllWithoutFetch());
    }

    // Решение через JOIN FETCH
    public List<PhotoSessionDto> getAllWithJoinFetch() {
        return mapToDtoList(repository.findAllWithJoinFetch());
    }

    // Решение через EntityGraph
    public List<PhotoSessionDto> getAllWithEntityGraph() {
        return mapToDtoList(repository.findAllWithEntityGraph());
    }

    // Сохранение нескольких связанных сущностей БЕЗ @Transactional (для демонстрации)
    public PhotoSessionDto createWithRelatedNoTransaction(PhotoSessionDto dto,
                                                          Long clientId,
                                                          Long photographerId,
                                                          Long serviceId) {
        return self.createWithRelatedInternal(dto, clientId, photographerId, serviceId);
    }

    // Сохранение нескольких связанных сущностей С @Transactional
    @Transactional
    public PhotoSessionDto createWithRelatedWithTransaction(PhotoSessionDto dto,
                                                            Long clientId,
                                                            Long photographerId,
                                                            Long serviceId) {
        return self.createWithRelatedInternal(dto, clientId, photographerId, serviceId);
    }

    // Внутренний метод с общей логикой
    private PhotoSessionDto createWithRelatedInternal(PhotoSessionDto dto,
                                                      Long clientId,
                                                      Long photographerId,
                                                      Long serviceId) {
        PhotoSession session = prepareSessionWithRelated(dto, clientId,
                photographerId, serviceId);
        PhotoSession saved = repository.save(session);

        // Валидация
        validateSession(saved);

        return mapper.toDto(saved);
    }

    // Вспомогательные приватные методы

    private PhotoSessionDto savePhotoSession(PhotoSession session, PhotoSessionDto dto) {
        mapper.updateEntity(session, dto);
        setRelatedEntities(session, dto);
        return mapper.toDto(repository.save(session));
    }

    private List<PhotoSessionDto> mapToDtoList(List<PhotoSession> sessions) {
        return sessions.stream()
                .map(mapper::toDto)
                .toList();
    }

    private PhotoSession prepareSessionWithRelated(PhotoSessionDto dto,
                                                   Long clientId,
                                                   Long photographerId,
                                                   Long serviceId) {
        PhotoSession session = mapper.toEntity(dto);

        setEntityIfPresent(clientId,
                id -> session.setClient(clientRepository.getReferenceById(id)),
                () -> clientRepository.findById(clientId)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Client not found with id: " + clientId)));

        setEntityIfPresent(photographerId,
                id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)),
                () -> photographerRepository.findById(photographerId)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Photographer not found with id: " + photographerId)));

        setEntityIfPresent(serviceId,
                id -> session.setService(serviceRepository.getReferenceById(id)),
                () -> serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Service not found with id: " + serviceId)));

        return session;
    }

    private void setEntityIfPresent(Long id,
                                    LongConsumer referenceSetter,
                                    Supplier<Object> entitySupplier) {
        if (id != null) {
            try {
                referenceSetter.accept(id);
            } catch (Exception e) {
                if (entitySupplier != null) {
                    entitySupplier.get();
                }
            }
        }
    }

    private void validateSession(PhotoSession session) {
        if (session.getPrice() > 1000) {
            throw new IllegalStateException("Price too high: " + session.getPrice());
        }
    }

    private void setRelatedEntities(PhotoSession session, PhotoSessionDto dto) {
        setEntityIfPresent(dto.getClientId(),
                id -> session.setClient(clientRepository.getReferenceById(id)), null);
        setEntityIfPresent(dto.getPhotographerId(),
                id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)),
                null);
        setEntityIfPresent(dto.getServiceId(),
                id -> session.setService(serviceRepository.getReferenceById(id)), null);
    }
}