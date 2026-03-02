package com.example.photostudio.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    WEDDING("Свадебная съемка", 5000.0, 240),
    PORTRAIT("Портретная съемка", 3000.0, 120),
    PRODUCT("Предметная съемка", 2500.0, 90),
    CORPORATE("Корпоративная съемка", 4000.0, 180),
    FAMILY("Семейная съемка", 3500.0, 150);

    private final String displayName;
    private final Double basePrice;
    private final Integer durationMinutes;
}