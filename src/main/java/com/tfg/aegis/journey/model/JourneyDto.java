package com.tfg.aegis.journey.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JourneyDto {
    private Long id;
    private Enums.JourneyState state;
    private Enums.JourneyType journeyType;
    private LocalDateTime iniDate;
    private LocalDateTime endDate;
    private Long groupId;
    private Set<Long> participantsIds;
    private Long companionRequestId;
}
