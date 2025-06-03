package com.tfg.aegis.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PersonDto {
    private Long id;
    private String clerkId;
    private String name;
    private String phone;
    private String email;
    private Date dateOfBirth;
}