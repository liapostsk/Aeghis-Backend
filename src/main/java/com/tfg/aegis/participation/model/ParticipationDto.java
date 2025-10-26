package com.tfg.aegis.participation.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ParticipationDto {
    private Long id;
    private Long journeyId;
    private Long userId;
    private Boolean sharedLocation;
    private Long destinationId;
    private Long sourceId;
    private Set<Long> lastLocationsIds;
    private Enums.ParticipationState state;
    private LocalDateTime arrivalTime;
    private Long valoracionId;
}
