package com.tfg.aegis.user.model;

import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDto {
    private Long id;
    private String clerkId;
    private String name;
    private String phone;
    private String email;
    private Date dateOfBirth;
    private String image;
    private Boolean acceptedPrivacyPolicy;
    private Set<EmergencyContactDto> emergencyContacts;
    private Set<ExternalContactDto> externalContacts;
    private Set<SafeLocationDto> safeLocations;
    private Boolean verify;
    private Enums.TypeRole role;
}
