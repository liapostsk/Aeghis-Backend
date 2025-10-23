package com.tfg.aegis.location.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDto {
    private Long id;
    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;
}