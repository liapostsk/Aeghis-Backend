package com.tfg.aegis.emergencycontact.mapper;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;

public interface EmergencyContactMapper {
    EmergencyContact toEntity(EmergencyContactDto dto);

    EmergencyContactDto toDto(EmergencyContact emergencyContact);
}
