package com.example.photostudio.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSession {
    private Long id;
    private String clientLastName;
    private String clientName;
    private String clientPhone;
    private LocalDateTime photoSessionDate;
    private double price;
    private String photographer;
    private String status;
}