package com.tfg.aegis.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationDto {
    private Long id;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String name;
}