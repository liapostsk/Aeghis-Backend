package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.EmergencyContactEnum;
import lombok.Data;

@Data
public class EmergencyContactDto {
    private Long id;
    private Long ownerId;
    private Long contactId;
    private String relation;
    private EmergencyContactEnum.Status status;
}
