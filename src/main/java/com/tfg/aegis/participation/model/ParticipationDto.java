package com.tfg.aegis.participation.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipationDto {
    private Long id;
    private Long journeyId;
    private Long userId;
    private Boolean shareLocation;
    private Long destinationId;
    private Long sourceId;
    private Long lasLocationId;
    private Enums.EstadoParticipacion state;
    private LocalDateTime arrivalTime;
    private Long valoracionId;
}
