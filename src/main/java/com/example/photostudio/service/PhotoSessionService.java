package com.example.photostudio.service;

import com.example.photostudio.cache.PhotoSessionCache;
import com.example.photostudio.cache.PhotoSessionQueryKey;
import com.example.photostudio.dto.PhotoSessionCreateDto;
import com.example.photostudio.dto.PhotoSessionDto;
import com.example.photostudio.dto.PhotoSessionFilterDto;
import com.example.photostudio.dto.PhotoSessionUpdateDto;
import com.example.photostudio.mapper.PhotoSessionMapper;
import com.example.photostudio.model.Client;
import com.example.photostudio.model.Photographer;
import com.example.photostudio.model.PhotoService;
import com.example.photostudio.model.PhotoSession;
import com.example.photostudio.repository.ClientRepository;
import com.example.photostudio.repository.PhotoSessionRepository;
import com.example.photostudio.repository.PhotographerRepository;
import com.example.photostudio.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PhotoSessionService {
    private static final Logger LOG = LoggerFactory.getLogger(PhotoSessionService.class);

    private static final String CLIENT_NOT_FOUND = "Клиент не найден";
    private static final String PHOTOGRAPHER_NOT_FOUND = "Фотограф не найден";
    private static final String SERVICE_NOT_FOUND = "Услуга не найдена";
    private static final String WITH_ID = " с id: ";
    private static final String TX_DEMO_START = "ТРАНЗАКЦИЯ ДЕМО - СТАРТ";
    private static final String TX_DEMO_END_COMMITTED = "ТРАНЗАКЦИЯ ДЕМО - КОНЕЦ (зафиксировано)";
    private static final String NO_TX_DEMO_START = "БЕЗ ТРАНЗАКЦИИ ДЕМО - СТАРТ";
    private static final String NO_TX_DEMO_END_SUCCESS = "БЕЗ ТРАНЗАКЦИИ ДЕМО - КОНЕЦ (успешно)";
    private static final String N_PLUS_1_DEMO = "N+1 ПРОБЛЕМА ДЕМО";
    private static final String ENTITY_GRAPH_DEMO = "ENTITYGRAPH РЕШЕНИЕ ДЕМО";

    private final PhotoSessionRepository repository;
    private final PhotoSessionMapper mapper;
    private final ClientRepository clientRepository;
    private final PhotographerRepository photographerRepository;
    private final ServiceRepository serviceRepository;
    private final PhotoSessionCache photoSessionCache;

    public List<PhotoSessionDto> getAllPhotoSessions() {
        LOG.info("Получение всех фотосессий");
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public PhotoSessionDto getPhotoSessionById(Long id) {
        LOG.info("Получение фотосессии по ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    public List<PhotoSessionDto> getPhotoSessionsByClientId(Long clientId) {
        LOG.info("Получение фотосессий по ID клиента: {}", clientId);
        return repository.findByClientId(clientId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public PhotoSessionDto createPhotoSession(PhotoSessionCreateDto createDto) {
        LOG.info("Создание новой фотосессии");

        Client client = findClientById(createDto.getClientId());
        Photographer photographer = findPhotographerById(createDto.getPhotographerId());
        PhotoService service = findServiceById(createDto.getServiceId());

        PhotoSession session = PhotoSession.builder()
                .date(createDto.getDate())
                .client(client)
                .photographer(photographer)
                .service(service)
                .totalPrice(calculateTotalPrice(service, photographer))
                .build();

        PhotoSessionDto saved = mapper.toDto(repository.save(session));
        photoSessionCache.invalidateAll();
        LOG.info("Кэш очищен после создания фотосессии с ID: {}", saved.getId());

        return saved;
    }

    @Transactional
    public PhotoSessionDto updatePhotoSession(Long id, PhotoSessionUpdateDto updateDto) {
        LOG.info("Обновление фотосессии с ID: {}", id);

        return repository.findById(id)
                .map(session -> {
                    session.setDate(updateDto.getDate());

                    if (updateDto.getServiceId() != null) {
                        PhotoService service = findServiceById(updateDto.getServiceId());
                        session.setService(service);
                        session.setTotalPrice(calculateTotalPrice(service, session.getPhotographer()));
                    }

                    PhotoSessionDto updated = mapper.toDto(repository.save(session));
                    photoSessionCache.invalidateAll();
                    LOG.info("Кэш очищен после обновления фотосессии с ID: {}", id);

                    return updated;
                })
                .orElse(null);
    }

    @Transactional
    public boolean deletePhotoSession(Long id) {
        LOG.info("Удаление фотосессии с ID: {}", id);

        return repository.findById(id)
                .map(session -> {
                    String clientPhone = session.getClient().getPhone();
                    String clientName = session.getClient().getFirstName() + " " + session.getClient().getLastName();
                    String photographerName = session.getPhotographer().getFirstName() +
                            " " + session.getPhotographer().getLastName();

                    repository.delete(session);

                    photoSessionCache.invalidateByClientPhone(clientPhone);
                    photoSessionCache.invalidateByClientName(clientName);
                    photoSessionCache.invalidateByPhotographerName(photographerName);

                    LOG.info("Кэш очищен после удаления фотосессии с ID: {}", id);
                    return true;
                })
                .orElse(false);
    }

    public void demonstrateNPlus1Problem() {
        demonstrateFetchProblem(N_PLUS_1_DEMO, false);
    }

    public void demonstrateEntityGraphSolution() {
        demonstrateFetchProblem(ENTITY_GRAPH_DEMO, true);
    }

    @Transactional
    public void createWithRelatedWithTransaction(PhotoSessionCreateDto dto) {
        processPhotoSessionCreation(dto, "С ТРАНЗАКЦИЕЙ", true);
    }

    public void createWithRelatedWithoutTransaction(PhotoSessionCreateDto dto) {
        processPhotoSessionCreation(dto, "БЕЗ ТРАНЗАКЦИИ", false);
    }

    public List<PhotoSessionDto> getSessionsWithFiltersJpql(PhotoSessionFilterDto filter) {
        LOG.info("Поиск фотосессий с фильтрацией JPQL");

        List<PhotoSession> sessions = repository.findSessionsWithFiltersJpql(
                filter.clientName(),
                filter.photographerName(),
                filter.phone()
        );

        LOG.info("Найдено фотосессий: {}", sessions.size());
        return sessions.stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<PhotoSessionDto> getSessionsWithFiltersNative(PhotoSessionFilterDto filter) {
        LOG.info("Поиск фотосессий с фильтрацией (Native Query)");

        List<PhotoSession> sessions = repository.findSessionsWithFiltersNative(
                filter.clientName(),
                filter.photographerName(),
                filter.phone()
        );

        LOG.info("Найдено фотосессий : {}", sessions.size());
        return sessions.stream()
                .map(mapper::toDto)
                .toList();
    }

    public Page<PhotoSessionDto> getSessionsWithFiltersPaged(PhotoSessionFilterDto filter) {
        LOG.info("Поиск фотосессий с пагинацией: page={}, size={}", filter.page(), filter.size());

        Pageable pageable = filter.toPageable();

        Page<PhotoSession> sessionPage = repository.findSessionsWithFiltersPaged(
                filter.clientName(),
                filter.photographerName(),
                filter.phone(),
                pageable
        );

        LOG.info("Найдено фотосессий: {}, всего страниц: {}", sessionPage.getNumberOfElements(),
                sessionPage.getTotalPages());

        return sessionPage.map(mapper::toDto);
    }

    public Page<PhotoSessionDto> getSessionsWithCache(PhotoSessionFilterDto filter) {
        LOG.info("Поиск фотосессий с использованием кэша: page={}, size={}", filter.page(), filter.size());

        PhotoSessionQueryKey cacheKey = new PhotoSessionQueryKey(
                filter.clientName(),
                filter.photographerName(),
                filter.phone(),
                filter.page(),
                filter.size()
        );

        Page<PhotoSessionDto> cachedResult = photoSessionCache.get(cacheKey);
        if (cachedResult != null) {
            LOG.info("Данные получены из кэша");
            return cachedResult;
        }

        LOG.info("Данных нет в кэше, выполняем запрос к БД");
        Page<PhotoSessionDto> result = getSessionsWithFiltersPaged(filter);
        photoSessionCache.put(cacheKey, result);

        return result;
    }

    public int getCacheSize() {
        int size = photoSessionCache.getCacheSize();
        LOG.info("Текущий размер кэша: {}", size);
        return size;
    }

    private void demonstrateFetchProblem(String demoType, boolean useEntityGraph) {
        LOG.info("{} - СТАРТ", demoType);

        List<PhotoSession> sessions = useEntityGraph
                ? repository.findAllWithEntityGraph()
                : repository.findAllWithoutFetch();

        LOG.info("Загружено {} сессий", sessions.size());

        int queryNumber = 1;
        for (PhotoSession session : sessions) {
            if (!useEntityGraph) {
                queryNumber++;
            }

            String clientName = session.getClient().getFirstName();
            String photographerName = session.getPhotographer().getFirstName();
            String serviceName = session.getService().getServiceType().getDisplayName();

            if (useEntityGraph) {
                LOG.info("Сессия {}: клиент={}, фотограф={}, услуга={} (все загружено в основном запросе)",
                        session.getId(), clientName, photographerName, serviceName);
            } else {
                LOG.info("Запрос {}: Сессия {}: клиент={}, фотограф={}, услуга={}",
                        queryNumber, session.getId(), clientName, photographerName, serviceName);
            }
        }

        String totalQueries = useEntityGraph
                ? "всего запросов: 1"
                : "всего запросов: 1 + " + sessions.size() + "*3";
        LOG.info("{} - КОНЕЦ ({})", demoType, totalQueries);
    }

    private void processPhotoSessionCreation(PhotoSessionCreateDto dto, String type, boolean isTransactional) {
        String startMessage = isTransactional ? TX_DEMO_START : NO_TX_DEMO_START;
        LOG.info(startMessage);

        Client client = findClientById(dto.getClientId());
        Photographer photographer = findPhotographerById(dto.getPhotographerId());
        PhotoService service = findServiceById(dto.getServiceId());

        PhotoSession session = buildPhotoSession(dto, client, photographer, service);
        PhotoSession saved = repository.save(session);

        LOG.info("Сохранена сессия id={} totalPrice={} ({})", saved.getId(), saved.getTotalPrice(), type);

        if (dto.getServiceId() == 3) {
            String errorMessage = "Демонстрационная ошибка: услуга с ID=3 запрещена";
            LOG.error("{} ДЕМО - ОШИБКА: {}", type, errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        String endMessage = isTransactional ? TX_DEMO_END_COMMITTED : NO_TX_DEMO_END_SUCCESS;
        LOG.info(endMessage);
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(CLIENT_NOT_FOUND + WITH_ID + id));
    }

    private Photographer findPhotographerById(Long id) {
        return photographerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(PHOTOGRAPHER_NOT_FOUND + WITH_ID + id));
    }

    private PhotoService findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(SERVICE_NOT_FOUND + WITH_ID + id));
    }

    private Double calculateTotalPrice(PhotoService service, Photographer photographer) {
        return service.getServiceType().getBasePrice() + (photographer.getHourlyRate() * 2);
    }

    private PhotoSession buildPhotoSession(PhotoSessionCreateDto dto,
                                           Client client,
                                           Photographer photographer,
                                           PhotoService service) {
        return PhotoSession.builder()
                .date(dto.getDate())
                .client(client)
                .photographer(photographer)
                .service(service)
                .totalPrice(calculateTotalPrice(service, photographer))
                .build();
    }
}