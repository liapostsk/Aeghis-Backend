package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.CompanionRequestEnums;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanionRequestDto {
    private Long id;
    private LocationDto source;
    private LocationDto destination;
    private LocalDateTime aproxHour;
    private String description;
    private CompanionRequestEnums.RequestStatus state;
    private LocalDateTime creationDate;
    private UserDto creator;
    private UserDto companion;
    private String companionMessage;
    private Long companionGroupId;
    private Long creatorTrackingGroup;
    private Long companionTrackingGroup;
    private Long trayectoId;
}
