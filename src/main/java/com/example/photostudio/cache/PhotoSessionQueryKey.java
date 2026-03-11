package com.example.photostudio.cache;

public record PhotoSessionQueryKey(
        String clientName,
        String photographerName,
        String phone,
        Integer page,
        Integer size
) {
}