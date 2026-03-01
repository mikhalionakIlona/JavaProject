package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
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
    private static final Logger LOG = LoggerFactory.getLogger(PhotoSessionService.class);

    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository;

    public List<PhotoSessionDto> getAllPhotoSessions(String clientName) {
        List<PhotoSession> sessions;
        if (clientName != null && !clientName.isEmpty()) {
            sessions = repository.findByClientNameIgnoreCase(clientName);
        } else {
            sessions = repository.findAll();
        }
        return sessions.stream().map(mapper::toDto).toList();
    }

    public PhotoSessionDto getPhotoSessionById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    public PhotoSessionDto createPhotoSession(PhotoSessionDto dto) {
        PhotoSession session = mapper.toEntity(dto);
        setRelatedEntities(session, dto);
        return mapper.toDto(repository.save(session));
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

    public void demonstrateNPlus1Problem() {
        LOG.info("N+1 DEMO - START");
        List<PhotoSession> sessions = repository.findAllWithoutFetch();
        LOG.info("Loaded {} sessions", sessions.size());

        int queryNumber = 1;
        for (PhotoSession session : sessions) {
            queryNumber++;
            String clientName = session.getClient().getFirstName();
            LOG.info("Query {}: Session {} client {}", queryNumber, session.getId(), clientName);
        }
        LOG.info("N+1 DEMO - END (total queries: 1 + {})", sessions.size());
    }

    public void demonstrateEntityGraphSolution() {
        LOG.info("ENTITYGRAPH DEMO - START");
        List<PhotoSession> sessions = repository.findAllWithEntityGraph();
        LOG.info("Loaded {} sessions with graph", sessions.size());

        for (PhotoSession session : sessions) {
            String clientName = session.getClient().getFirstName();
            LOG.info("Session {} client {} (loaded in main query)", session.getId(), clientName);
        }
        LOG.info("ENTITYGRAPH DEMO - END (total queries: 1)");
    }

    public void createWithRelatedWithoutTransaction(PhotoSessionDto dto,
                                                    Long clientId,
                                                    Long photographerId,
                                                    Long serviceId) {
        LOG.info("NO-TX DEMO - START");
        PhotoSession session = prepareSessionWithRelated(dto, clientId, photographerId, serviceId);
        PhotoSession saved = repository.save(session);
        LOG.info("Saved session id={} price={}", saved.getId(), saved.getPrice());

        if (saved.getPrice() > 1000) {
            LOG.error("NO-TX DEMO - ERROR: price too high, but data already saved");
            throw new IllegalStateException("Price too high - data remains in DB");
        }
        LOG.info("NO-TX DEMO - END (success)");
    }

    @Transactional
    public void createWithRelatedWithTransaction(PhotoSessionDto dto,
                                                 Long clientId,
                                                 Long photographerId,
                                                 Long serviceId) {
        LOG.info("TX DEMO - START");
        PhotoSession session = prepareSessionWithRelated(dto, clientId, photographerId, serviceId);
        PhotoSession saved = repository.save(session);
        LOG.info("Saved in transaction id={} price={}", saved.getId(), saved.getPrice());

        if (saved.getPrice() > 1000) {
            LOG.error("TX DEMO - ERROR: price too high, rolling back transaction");
            throw new IllegalStateException("Price too high - transaction rolled back");
        }
        LOG.info("TX DEMO - END (committed)");
    }

    private PhotoSession prepareSessionWithRelated(PhotoSessionDto dto,
                                                   Long clientId,
                                                   Long photographerId,
                                                   Long serviceId) {
        PhotoSession session = mapper.toEntity(dto);

        if (clientId != null) {
            setEntityIfPresent(clientId,
                    id -> session.setClient(clientRepository.getReferenceById(id)),
                    () -> clientRepository.findById(clientId)
                            .orElseThrow(() -> new NoSuchElementException("Client " + clientId + "no found")));
        }
        if (photographerId != null) {
            setEntityIfPresent(photographerId,
                    id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)),
                    () -> photographerRepository.findById(photographerId)
                            .orElseThrow(() -> new NoSuchElementException("Photographer "
                                    + photographerId + " not found")));
        }
        if (serviceId != null) {
            setEntityIfPresent(serviceId,
                    id -> session.setService(serviceRepository.getReferenceById(id)),
                    () -> serviceRepository.findById(serviceId)
                            .orElseThrow(() -> new NoSuchElementException("Service " + serviceId + " not found")));
        }
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

    private void setRelatedEntities(PhotoSession session, PhotoSessionDto dto) {
        if (dto.getClientId() != null) {
            setEntityIfPresent(dto.getClientId(),
                    id -> session.setClient(clientRepository.getReferenceById(id)), null);
        }
        if (dto.getPhotographerId() != null) {
            setEntityIfPresent(dto.getPhotographerId(),
                    id -> session.setPhotographerEntity(photographerRepository.getReferenceById(id)), null);
        }
        if (dto.getServiceId() != null) {
            setEntityIfPresent(dto.getServiceId(),
                    id -> session.setService(serviceRepository.getReferenceById(id)), null);
        }
    }
}