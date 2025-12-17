package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.UserEnums;
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
    private UserEnums.VerificationStatus verify;
    private UserEnums.TypeRole role;
    private Set<Long> companionRequestsCreatedIds;
    private Set<Long> companionRequestsAcceptedIds;
}
