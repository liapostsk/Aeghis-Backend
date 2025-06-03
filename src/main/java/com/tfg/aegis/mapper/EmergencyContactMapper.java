package com.tfg.aegis.mapper;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.exception.user.UserNotFoundException;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.model.UserDto;

public class EmergencyContactMapper {

    private EmergencyContactMapper() {
        // Prevent instantiation
    }

    private static UserRepository userRepository;

    public static EmergencyContact toEntity(EmergencyContactDto dto, User user) {
        EmergencyContact contact = new EmergencyContact();
        contact.setConfirmed(true);
        if (dto.getEmergencyContactId() != null) {
            // If the contact is registered, we set the contact field
            User emergencyContactUser = userRepository.findById(dto.getEmergencyContactId())
                    .orElseThrow(() -> new UserNotFoundException(dto.getEmergencyContactId()));
            contact.setEmergencyContact(emergencyContactUser);
        }
        else {
            // If the contact is not registered, we set the name and phone
            contact.setName(dto.getName());
            contact.setPhone(dto.getPhone());
            contact.setRelation(dto.getRelation());
        }

        contact.setOwner(user);

        return contact;
    }

    public static EmergencyContactDto toDto(EmergencyContact contact, UserDto userDto) {
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setConfirmed(contact.isConfirmed());
        if (contact.getEmergencyContact() != null) {
            dto.setEmergencyContactId(contact.getEmergencyContact().getId());
        }
        else {
            dto.setName(contact.getName());
            dto.setPhone(contact.getPhone());
            dto.setRelation(contact.getRelation());
        }

        dto.setOwnerId(userDto.getId());
        return dto;
    }
}
