package com.tfg.aegis.trayecto.model;

import com.tfg.aegis.ubicacion.model.UbicacionDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrayectoDto {
    private Long id;
    private Enums.EstadoTrayecto estado;
    private Enums.TipoTrayecto tipoTrayecto;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private UbicacionDto sourcePoint;
    private UbicacionDto destino;
    // si necesitas, añade contadores o un resumen de participaciones
}
