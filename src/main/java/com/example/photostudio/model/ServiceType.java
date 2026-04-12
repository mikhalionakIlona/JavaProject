package com.example.photostudio.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    WEDDING("Свадебная съемка", 300.0),
    PORTRAIT("Портретная съемка", 100.0),
    PRODUCT("Предметная съемка", 50.0),
    CORPORATE("Корпоративная съемка", 200.0),
    FAMILY("Семейная съемка", 150.0);

    private final String displayName;
    private final Double basePrice;
}