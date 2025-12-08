package com.tfg.aegis.companionrequest.model;

import com.tfg.aegis.location.model.LocationDto;
import com.tfg.aegis.person.user.model.UserDto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanionRequestDto {
    private Long id;
    private LocationDto source;
    private LocationDto destination;
    private LocalDateTime aproxHour;
    private String description;
    private Enums.RequestStatus state;
    private LocalDateTime creationDate;
    private UserDto creator;
    private Long companionId;
    private Long companionGroupId;
    private Long trackingGroupId;
    private Long trayectoId;
}
