package com.tfg.aegis.emergencycontact.mapper;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.common.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class EmergencyContactMapperImpl implements EmergencyContactMapper {

    public EmergencyContact toEntity(EmergencyContactDto dto) {
        if (dto == null) return null;

        EmergencyContact entity = new EmergencyContact();
        entity.setId(dto.getId());

        if (dto.getOwnerId() != null) {
            User owner = new User();
            owner.setId(dto.getOwnerId());
            entity.setOwner(owner);
        }

        if (dto.getContactId() != null) {
            User contact = new User();
            contact.setId(dto.getContactId());
            entity.setContact(contact);
        }

        if (dto.getRelation() != null) {
            entity.setRelation(dto.getRelation());
        }

        entity.setStatus(dto.getStatus());

        return entity;
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
