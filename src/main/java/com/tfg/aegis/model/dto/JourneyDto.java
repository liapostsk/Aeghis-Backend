package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.JourneyEnums;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JourneyDto {
    private Long id;
    private JourneyEnums.JourneyState state;
    private JourneyEnums.JourneyType journeyType;
    private LocalDateTime iniDate;
    private LocalDateTime endDate;
    private Long groupId;
    private Set<Long> participantsIds;
    private Long companionRequestId;
}
