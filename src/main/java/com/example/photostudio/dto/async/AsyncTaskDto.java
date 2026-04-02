package com.example.photostudio.dto.async;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskDto {
    private String taskId;
    private String status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long clientId;
    private String clientName;
}