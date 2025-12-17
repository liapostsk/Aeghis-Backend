package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.emergencyContactEnum;
import lombok.Data;

@Data
public class EmergencyContactDto {
    private Long id;
    private Long ownerId;
    private Long contactId;
    private String relation;
    private emergencyContactEnum.Status status;
}
