package com.tfg.aegis.ubicacion.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UbicacionDto {
    private Long id;
    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;
}