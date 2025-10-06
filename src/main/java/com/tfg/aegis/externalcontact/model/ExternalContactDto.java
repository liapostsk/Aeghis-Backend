package com.tfg.aegis.externalcontact.model;

import lombok.Data;

@Data
public class ExternalContactDto {
    private Long id;
    private String name;
    private String phone;
    private String relation;
}
