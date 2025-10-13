package com.tfg.aegis.emergencycontact.mapper;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import org.springframework.stereotype.Component;

@Component
public class EmergencyContactMapperImpl implements EmergencyContactMapper {

    public EmergencyContact toEntity(EmergencyContactDto emergencyContactDto) {
        if (emergencyContactDto == null) return null;

        EmergencyContact emergencyContact = new EmergencyContact();

        emergencyContact.setRelation(emergencyContactDto.getRelation());
        emergencyContact.setStatus(emergencyContactDto.getStatus());
        return emergencyContact;
    }

    public EmergencyContactDto toDto(EmergencyContact entity) {
        if (entity == null) return null;

        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setId(entity.getId());
        dto.setOwnerId(entity.getOwner() != null ? entity.getOwner().getId() : null);
        dto.setContactId(entity.getContact() != null ? entity.getContact().getId() : null);
        dto.setRelation(entity.getRelation() != null ? entity.getRelation() : null);
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
