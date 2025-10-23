package com.tfg.aegis.journey.model;

import com.tfg.aegis.location.model.LocationDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JourneyDto {
    private Long id;
    private Long groupId;
    private Enums.EstadoTrayecto estado;
    private Enums.TipoTrayecto tipoTrayecto;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocationDto sourcePoint;
    private LocationDto destino;
    // si necesitas, añade contadores o un resumen de participaciones
}
