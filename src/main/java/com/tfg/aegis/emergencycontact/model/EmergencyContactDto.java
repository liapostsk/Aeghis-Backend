package com.tfg.aegis.emergencycontact.model;

import lombok.Data;

@Data
public class EmergencyContactDto {
    private Long id;
    private Long ownerId;
    private Long emergencyContactId;
    private String name;
    private String phone;
    private String relation;
    private boolean confirmed;
}
