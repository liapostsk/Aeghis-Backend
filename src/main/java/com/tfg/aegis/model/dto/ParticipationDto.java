package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.ParticipationEnums;
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
    private ParticipationEnums.ParticipationState state;
    private LocalDateTime arrivalTime;
}
