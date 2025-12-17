package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.dto.EmergencyContactDto;

public interface EmergencyContactMapper {
    EmergencyContact toEntity(EmergencyContactDto dto);

    EmergencyContactDto toDto(EmergencyContact emergencyContact);
}
