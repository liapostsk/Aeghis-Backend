package com.tfg.aegis.person.user.model;

import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.person.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.person.model.PersonDto;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDto extends PersonDto {
    private String clerkId;
    private String email;
    private Date dateOfBirth;
    private String image;
    private Boolean acceptedPrivacyPolicy;
    private Set<EmergencyContactDto> emergencyContacts;
    private Set<ExternalContactDto> externalContacts;
    private Set<SafeLocationDto> safeLocations;
    private Set<GroupDto> groups;
    private Boolean verify;
    private Enums.TypeRole role;
}
