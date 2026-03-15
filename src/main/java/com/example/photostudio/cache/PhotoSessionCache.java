package com.example.photostudio.cache;

import com.example.photostudio.dto.photosession.PhotoSessionDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PhotoSessionCache {

    private final ConcurrentHashMap<PhotoSessionQueryKey, Page<PhotoSessionDto>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("PhotoSessionCache инициализирован с HashMap индексом");
    }

    public Page<PhotoSessionDto> get(PhotoSessionQueryKey key) {
        Page<PhotoSessionDto> result = cache.get(key);
        if (result != null) {
            log.info("Кэш HIT для ключа: {}", key);
        } else {
            log.info("Кэш MISS для ключа: {}", key);
        }
        return result;
    }

    public void put(PhotoSessionQueryKey key, Page<PhotoSessionDto> data) {
        cache.put(key, data);
        log.info("Данные закэшированы для ключа: {}", key);
    }

    public void invalidateByClientPhone(String phone) {
        int removedCount = 0;
        for (PhotoSessionQueryKey key : cache.keySet()) {
            if (key.phone() != null && key.phone().equals(phone)) {
                cache.remove(key);
                removedCount++;
            }
        }
        log.info("Кэш очищен для телефона клиента: {}, удалено записей: {}", phone, removedCount);
    }

    public void invalidateByClientName(String clientName) {
        int removedCount = 0;
        for (PhotoSessionQueryKey key : cache.keySet()) {
            if (key.clientName() != null && key.clientName().equals(clientName)) {
                cache.remove(key);
                removedCount++;
            }
        }
        log.info("Кэш очищен для имени клиента: {}, удалено записей: {}", clientName, removedCount);
    }

    public void invalidateByPhotographerName(String photographerName) {
        int removedCount = 0;
        for (PhotoSessionQueryKey key : cache.keySet()) {
            if (key.photographerName() != null && key.photographerName().equals(photographerName)) {
                cache.remove(key);
                removedCount++;
            }
        }
        log.info("Кэш очищен для имени фотографа: {}, удалено записей: {}", photographerName, removedCount);
    }

    public void invalidateAll() {
        int size = cache.size();
        cache.clear();
        log.info("Кэш полностью очищен, удалено записей: {}", size);
    }

    public int getCacheSize() {
        return cache.size();
    }
}