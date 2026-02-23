package com.example.photostudio.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSessionResponseDto {
    private Long id;
    private String clientName;
    private String photoSessionDate;
    private String price;
    private String photographer;
    private String status;

    public PhotoSessionResponseDto(Long id, String clientName, 
        LocalDateTime photoSessionDate, double price,
            String photographer, String status) {
        this.id = id;
        this.clientName = clientName;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        this.photoSessionDate = photoSessionDate.format(formatter);
        this.price = String.format("%.0f руб", price);
        this.photographer = photographer;
        this.status = status;
    }
}