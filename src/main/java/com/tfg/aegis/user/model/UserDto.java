package com.tfg.aegis.user.model;

import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.person.dto.PersonDto;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDto extends PersonDto {
    private String image;
    private Boolean acceptedPrivacyPolicy;
    private Set<EmergencyContactDto> emergencyContacts;
    private Set<ExternalContactDto> externalContacts;
    private Set<SafeLocationDto> safeLocations;
    private Boolean verify;
}
