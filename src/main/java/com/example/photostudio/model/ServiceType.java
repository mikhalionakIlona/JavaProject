package com.example.photostudio.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    WEDDING("Свадебная съемка", 5000.0),
    PORTRAIT("Портретная съемка", 3000.0),
    PRODUCT("Предметная съемка", 2500.0),
    CORPORATE("Корпоративная съемка", 4000.0),
    FAMILY("Семейная съемка", 3500.0);

    private final String displayName;
    private final Double basePrice;
}