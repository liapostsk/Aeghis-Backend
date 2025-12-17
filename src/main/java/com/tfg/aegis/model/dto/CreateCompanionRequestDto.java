package com.tfg.aegis.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCompanionRequestDto {
    private Long sourceId;
    private Long destinationId;
    private LocalDateTime aproxHour;   // opcional
    private String description;        // opcional
}
