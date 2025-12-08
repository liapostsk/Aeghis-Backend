package com.tfg.aegis.companionrequest.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanionRequestDto {
    private Long id;
    private Long sourceId;
    private Long destinationId;
    private LocalDateTime aproxHour;
    private String description;
    private Enums.RequestStatus state;
    private LocalDateTime creationDate;
    private Long creatorId;
    private Long companionId;
    private Long companionGroupId;
    private Long trackingGroupId;
    private Long trayectoId;
}
