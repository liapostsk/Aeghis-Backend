package com.tfg.aegis.safelocation.model;

import lombok.Data;

@Data
public class SafeLocationDto {
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address; // opcional: para guardar la dirección del lugar
    private String type;
    private String distance; // opcional: para guardar la distancia al lugar
    private String externalId;
    private Long userId; // solo el ID para mapear desde el controller
}
