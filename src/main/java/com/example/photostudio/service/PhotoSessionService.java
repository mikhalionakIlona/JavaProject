package com.example.photostudio.service;

import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.dto.PhotoSessionCreateDto;
import com.example.photostudio.dto.PhotoSessionUpdateDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.model.Client;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private static final Logger LOG = LoggerFactory.getLogger(PhotoSessionService.class);

    private static final String CLIENT_NOT_FOUND = "Client not found";
    private static final String PHOTOGRAPHER_NOT_FOUND = "Photographer not found";
    private static final String SERVICE_NOT_FOUND = "Service not found";
    private static final String TX_DEMO_START = "TX DEMO - START";
    private static final String TX_DEMO_END_COMMITTED = "TX DEMO - END (committed)";
    private static final String NO_TX_DEMO_START = "NO-TX DEMO - START";
    private static final String NO_TX_DEMO_END_SUCCESS = "NO-TX DEMO - END (success)";
    private static final String N_PLUS_1_DEMO = "N+1 DEMO";
    private static final String ENTITY_GRAPH_DEMO = "ENTITYGRAPH DEMO";

    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository;

    public List<PhotoSessionDto> getAllPhotoSessions() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public PhotoSessionDto getPhotoSessionById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    public List<PhotoSessionDto> getPhotoSessionsByClientId(Long clientId) {
        return repository.findByClientId(clientId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public PhotoSessionDto createPhotoSession(PhotoSessionCreateDto createDto) {
        Client client = findClientById(createDto.getClientId());
        Photographer photographer = findPhotographerById(createDto.getPhotographerId());
        PhotoService service = findServiceById(createDto.getServiceId());

        PhotoSession session = PhotoSession.builder()
                .date(createDto.getSessionDate())
                .client(client)
                .photographer(photographer)
                .service(service)
                .totalPrice(calculateTotalPrice(service, photographer))
                .build();

        return mapper.toDto(repository.save(session));
    }

    @Transactional
    public PhotoSessionDto updatePhotoSession(Long id, PhotoSessionUpdateDto updateDto) {
        return repository.findById(id)
                .map(session -> {
                    session.setDate(updateDto.getSessionDate());

                    if (updateDto.getServiceId() != null) {
                        PhotoService service = findServiceById(updateDto.getServiceId());
                        session.setService(service);
                        session.setTotalPrice(calculateTotalPrice(service, session.getPhotographer()));
                    }

                    return mapper.toDto(repository.save(session));
                })
                .orElse(null);
    }

    @Transactional
    public boolean deletePhotoSession(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public void demonstrateNPlus1Problem() {
        demonstrateFetchProblem(N_PLUS_1_DEMO, false);
    }

    public void demonstrateEntityGraphSolution() {
        demonstrateFetchProblem(ENTITY_GRAPH_DEMO, true);
    }

    @Transactional
    public void createWithRelatedWithTransaction(PhotoSessionCreateDto dto) {
        processPhotoSessionCreation(dto, "TX", true);
    }

    public void createWithRelatedWithoutTransaction(PhotoSessionCreateDto dto) {
        processPhotoSessionCreation(dto, "NO-TX", false);
    }

    private void demonstrateFetchProblem(String demoType, boolean useEntityGraph) {
        LOG.info("{} - START", demoType);

        List<PhotoSession> sessions = useEntityGraph
                ? repository.findAllWithEntityGraph()
                : repository.findAllWithoutFetch();

        LOG.info("Loaded {} sessions", sessions.size());

        int queryNumber = 1;
        for (PhotoSession session : sessions) {
            if (!useEntityGraph) {
                queryNumber++;
            }

            String clientName = session.getClient().getFirstName();
            String photographerName = session.getPhotographer().getFirstName();
            String serviceName = session.getService().getServiceType().getDisplayName();

            if (useEntityGraph) {
                LOG.info("Session {}: client={}, photographer={}, service={} (all loaded in main query)",
                        session.getId(), clientName, photographerName, serviceName);
            } else {
                LOG.info("Query {}: Session {}: client={}, photographer={}, service={}",
                        queryNumber, session.getId(), clientName, photographerName, serviceName);
            }
        }

        String totalQueries = useEntityGraph
                ? "total queries: 1"
                : "total queries: 1 + " + sessions.size() + "*3";
        LOG.info("{} - END ({})", demoType, totalQueries);
    }

    private void processPhotoSessionCreation(PhotoSessionCreateDto dto, String type, boolean isTransactional) {
        String startMessage = isTransactional ? TX_DEMO_START : NO_TX_DEMO_START;
        LOG.info(startMessage);

        Client client = findClientById(dto.getClientId());
        Photographer photographer = findPhotographerById(dto.getPhotographerId());
        PhotoService service = findServiceById(dto.getServiceId());

        PhotoSession session = buildPhotoSession(dto, client, photographer, service);
        PhotoSession saved = repository.save(session);

        LOG.info("Saved session id={} totalPrice={} ({})",
                saved.getId(), saved.getTotalPrice(), type);

        if (dto.getServiceId() == 3) {
            String errorMessage = "Демонстрационная ошибка: услуга с ID=3 запрещена";
            LOG.error("{} DEMO - ERROR: {}", type, errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        String endMessage = isTransactional ? TX_DEMO_END_COMMITTED : NO_TX_DEMO_END_SUCCESS;
        LOG.info(endMessage);
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(CLIENT_NOT_FOUND + "with id: " + id));
    }

    private Photographer findPhotographerById(Long id) {
        return photographerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(PHOTOGRAPHER_NOT_FOUND + " with id: " + id));
    }

    private PhotoService findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_NOT_FOUND + " with id: " + id));
    }

    private Double calculateTotalPrice(PhotoService service, Photographer photographer) {
        return service.getServiceType().getBasePrice() + (photographer.getHourlyRate() * 2);
    }

    private PhotoSession buildPhotoSession(PhotoSessionCreateDto dto,
                                           Client client,
                                           Photographer photographer,
                                           PhotoService service) {
        return PhotoSession.builder()
                .date(dto.getSessionDate())
                .client(client)
                .photographer(photographer)
                .service(service)
                .totalPrice(calculateTotalPrice(service, photographer))
                .build();
    }
    
}