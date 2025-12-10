package com.tfg.aegis.safelocation.model;

import com.tfg.aegis.location.model.LocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SafeLocationDto extends LocationDto {
    private Long id;
    private String description;
    private String address; // opcional: para guardar la dirección del lugar
    private String type;
    private String distance; // opcional: para guardar la distancia al lugar
    private String externalId;
    private Long userId; // solo el ID para mapear desde el controller
}
