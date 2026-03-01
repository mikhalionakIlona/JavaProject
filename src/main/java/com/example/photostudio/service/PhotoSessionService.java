package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoSessionService.class);

    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository;

    public List<PhotoSessionDto> getAllPhotoSessions(String clientName) {
        List<PhotoSession> sessions;

        if (clientName != null && !clientName.isEmpty()) {
            sessions = repository.findByClientNameIgnoreCase(clientName);
            LOGGER.debug("Found {} sessions for client name: {}", sessions.size(), clientName);
        } else {
            sessions = repository.findAll();
            LOGGER.debug("Found all {} sessions", sessions.size());
        }

        return mapToDtoList(sessions);
    }

    public PhotoSessionDto getPhotoSessionById(Long id) {
        LOGGER.debug("Fetching photo session with id: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseGet(() -> {
                    LOGGER.warn("Photo session not found with id: {}", id);
                    return null;
                });
    }

    public PhotoSessionDto createPhotoSession(PhotoSessionDto dto) {
        LOGGER.info("Creating new photo session");
        PhotoSessionDto saved = savePhotoSession(new PhotoSession(), dto);
        LOGGER.info("Created photo session with id: {}", saved.getId());
        return saved;
    }

    public PhotoSessionDto updatePhotoSession(Long id, PhotoSessionDto dto) {
        LOGGER.info("Updating photo session with id: {}", id);
        return repository.findById(id)
                .map(session -> {
                    PhotoSessionDto updated = savePhotoSession(session, dto);
                    LOGGER.info("Updated photo session with id: {}", id);
                    return updated;
                })
                .orElseGet(() -> {
                    LOGGER.warn("Cannot update - photo session not found with id: {}", id);
                    return null;
                });
    }

    public boolean deletePhotoSession(Long id) {
        LOGGER.info("Attempting to delete photo session with id: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            LOGGER.info("Successfully deleted photo session with id: {}", id);
            return true;
        }
        LOGGER.warn("Cannot delete - photo session not found with id: {}", id);
        return false;
    }

    public void demonstrateNPlus1Problem() {
        LOGGER.info("=== START: ДЕМОНСТРАЦИЯ N+1 ПРОБЛЕМЫ ===");
        LOGGER.info("Этот метод выполнит 1 запрос для получения сессий и N запросов для каждого клиента");

        List<PhotoSession> sessions = repository.findAllWithoutFetch();
        LOGGER.info("Получено {} сессий без загрузки связанных сущностей", sessions.size());

        int sessionCount = 0;
        for (PhotoSession session : sessions) {
            sessionCount++;
            String clientName = getClientName(session);
            LOGGER.info("Обработка сессии #{}: ID={}, Клиент='{}' - этот запрос к БД выполняется сейчас",
                    sessionCount, session.getId(), clientName);
        }

        LOGGER.info("=== FINISH: Демонстрация N+1 завершена. Выполнено 1 + {} запросов ===", sessions.size());
    }

    public void demonstrateEntityGraphSolution() {
        LOGGER.info("=== START: РЕШЕНИЕ ЧЕРЕЗ @EntityGraph ===");
        LOGGER.info("Этот метод выполнит 1 запрос с EntityGraph, загружающий все связанные сущности");

        List<PhotoSession> sessions = repository.findAllWithEntityGraph();
        LOGGER.info("Получено {} фотосессий с загруженными клиентами через EntityGraph", sessions.size());

        int sessionCount = 0;
        for (PhotoSession session : sessions) {
            sessionCount++;
            String clientName = getClientName(session);
            LOGGER.info("Обработка сессии #{}: ID={}, Клиент='{}' - данные уже загружены, доп. запросов нет",
                    sessionCount, session.getId(), clientName);
        }

        LOGGER.info("=== FINISH: Решение с EntityGraph выполнено. Всего 1 запрос к БД ===");
    }

    @Transactional
    public PhotoSessionDto createWithRelatedWithTransaction(PhotoSessionDto dto,
                                                            Long clientId,
                                                            Long photographerId,
                                                            Long serviceId) {
        LOGGER.info("Creating photo session with transaction - clientId: {}, photographerId: {}, serviceId: {}",
                clientId, photographerId, serviceId);

        PhotoSession session = prepareSessionWithRelated(dto, clientId,
                photographerId, serviceId);
        PhotoSession saved = repository.save(session);

        LOGGER.info("Photo session saved with id: {}, price: {}", saved.getId(), saved.getPrice());
        validateSession(saved);

        LOGGER.info("Transaction successful for photo session id: {}", saved.getId());
        return mapper.toDto(saved);
    }

    private PhotoSessionDto savePhotoSession(PhotoSession session, PhotoSessionDto dto) {
        mapper.updateEntity(session, dto);
        setRelatedEntities(session, dto);
        PhotoSession saved = repository.save(session);
        LOGGER.debug("Saved photo session with id: {}", saved.getId());
        return mapper.toDto(saved);
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
        LOGGER.debug("Preparing session with related entities");

        if (clientId != null) {
            LOGGER.debug("Setting client with id: {}", clientId);
            setEntityIfPresent(clientId,
                    id -> session.setClient(clientRepository.getReferenceById(id)),
                    () -> clientRepository.findById(clientId)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Client not found with id: " + clientId)));
        }

        if (photographerId != null) {
            LOGGER.debug("Setting photographer with id: {}", photographerId);
            setEntityIfPresent(photographerId,
                    id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)),
                    () -> photographerRepository.findById(photographerId)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Photographer not found with id: " + photographerId)));
        }

        if (serviceId != null) {
            LOGGER.debug("Setting service with id: {}", serviceId);
            setEntityIfPresent(serviceId,
                    id -> session.setService(serviceRepository.getReferenceById(id)),
                    () -> serviceRepository.findById(serviceId)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Service not found with id: " + serviceId)));
        }
        return session;
    }

    private void setEntityIfPresent(Long id,
                                    LongConsumer referenceSetter,
                                    Supplier<Object> entitySupplier) {
        if (id != null) {
            try {
                referenceSetter.accept(id);
                LOGGER.trace("Successfully set reference for id: {}", id);
            } catch (Exception e) {
                LOGGER.warn("Error setting reference for id: {} - {}", id, e.getMessage());
                if (entitySupplier != null) {
                    entitySupplier.get();
                    LOGGER.debug("Verified entity existence for id: {}", id);
                }
            }
        }
    }

    private void validateSession(PhotoSession session) {
        if (session.getPrice() > 1000) {
            LOGGER.error("Price validation failed for session id: {} - price {} exceeds limit 1000",
                    session.getId(), session.getPrice());
            throw new IllegalStateException("Price too high: " + session.getPrice());
        }
        LOGGER.debug("Price validation passed for session id: {}", session.getId());
    }

    private void setRelatedEntities(PhotoSession session, PhotoSessionDto dto) {
        LOGGER.debug("Setting related entities from DTO");

        if (dto.getClientId() != null) {
            LOGGER.debug("Setting client from DTO with id: {}", dto.getClientId());
            setEntityIfPresent(dto.getClientId(),
                    id -> session.setClient(clientRepository.getReferenceById(id)), null);
        }

        if (dto.getPhotographerId() != null) {
            LOGGER.debug("Setting photographer from DTO with id: {}", dto.getPhotographerId());
            setEntityIfPresent(dto.getPhotographerId(),
                    id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)),
                    null);
        }

        if (dto.getServiceId() != null) {
            LOGGER.debug("Setting service from DTO with id: {}", dto.getServiceId());
            setEntityIfPresent(dto.getServiceId(),
                    id -> session.setService(serviceRepository.getReferenceById(id)), null);
        }
    }

    private String getClientName(PhotoSession session) {
        Client client = session.getClient();
        return client != null ? client.getFirstName() : "Unknown";
    }
}