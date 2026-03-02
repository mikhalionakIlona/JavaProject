package com.example.photostudio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSessionDto {
    private Long id;
    private LocalDateTime sessionDate;
    private Double totalPrice;
    private Long clientId;
    private String clientName;
    private String clientLastName;
    private Long photographerId;
    private String photographerName;
    private Long serviceId;
    private String serviceName;
}