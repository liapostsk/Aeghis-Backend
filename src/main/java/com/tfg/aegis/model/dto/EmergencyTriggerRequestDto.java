package com.tfg.aegis.model.dto;

import lombok.Data;

@Data
public class EmergencyTriggerRequestDto {
    Double latitude;
    Double longitude;
    String message;
}