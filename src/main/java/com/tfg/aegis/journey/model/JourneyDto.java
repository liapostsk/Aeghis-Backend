package com.tfg.aegis.journey.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JourneyDto {
    private Long id;
    private Long groupId;
    private Enums.JourneyState state;
    private Enums.JourneyType journeyType;
    private LocalDateTime iniDate;
    private LocalDateTime endDate;
    private Set<Long> participantsIds;
}
