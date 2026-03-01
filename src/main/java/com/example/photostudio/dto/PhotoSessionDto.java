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
    private String clientName;
    private String clientLastName;
    private String clientPhone;
    private LocalDateTime photoSessionDate;
    private Double price;
    private String photographer;
    private String status;
    private Long clientId;
    private Long photographerId;
    private Long serviceId;
}