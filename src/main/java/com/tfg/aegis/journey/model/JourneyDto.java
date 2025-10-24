package com.tfg.aegis.journey.model;

import com.tfg.aegis.location.model.LocationDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JourneyDto {
    private Long id;
    private Long groupId;
    private Enums.EstadoTrayecto state;
    private Enums.TipoTrayecto journeyType;
    private LocalDateTime iniDate;
    private LocalDateTime endDate;
    private LocationDto sourcePoint;
    private LocationDto destination;
    private Set<Long> participationIds;
}
