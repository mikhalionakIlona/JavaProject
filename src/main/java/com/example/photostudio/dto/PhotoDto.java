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
public class PhotoDto {
    private Long id;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadDate;
    private Long sessionId;
}