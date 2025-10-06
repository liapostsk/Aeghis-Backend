package com.tfg.aegis.emergencycontact.model;

import lombok.Data;

@Data
public class EmergencyContactDto {
    private Long id;
    private Long ownerId;
    private Long contactId;
    private String relation;
    private Enums.Status status;
}
