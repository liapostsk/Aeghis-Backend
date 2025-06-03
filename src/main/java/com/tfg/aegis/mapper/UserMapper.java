package com.tfg.aegis.mapper;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import com.tfg.aegis.user.model.UserDto;
import com.tfg.aegis.user.model.User;

import java.util.HashSet;
import java.util.Set;

public class UserMapper {

    private UserMapper() {
        // Previene instanciación
    }

    public static User toEntity(UserDto dto) {
        User user = new User();
        user.setClerkId(dto.getClerkId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setAcceptedPrivacyPolicy(dto.getAcceptedPrivacyPolicy());
        user.setVerify(dto.getVerify());
        user.setImage(dto.getImage());
        // List emergencyContacts con el mapper
        Set<EmergencyContact> contacts = new HashSet<>();
        if (dto.getEmergencyContacts() != null) {
            for (EmergencyContactDto contactDto : dto.getEmergencyContacts()) {
                // Convertimos el EmergencyContactDto a EmergencyContact con el mapper
                EmergencyContact emergencyContact = EmergencyContactMapper.toEntity(contactDto, user);

                contacts.add(emergencyContact);
            }
        }
        user.setEmergencyContacts(contacts);

        // List safeLocations con el mapper
        Set<SafeLocation> safeLocations = new HashSet<>();
        if (dto.getSafeLocations() != null) {
            for (SafeLocationDto safeLocationDto : dto.getSafeLocations()) {
                // Convertimos el SafeLocationDto a SafeLocation con el mapper
                SafeLocation safeLocation = SafeLocationMapper.toEntity(safeLocationDto, user);

                safeLocations.add(safeLocation);
            }
        }
        user.setSafeLocations(safeLocations);

        return user;
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setClerkId(user.getClerkId());
        dto.setAcceptedPrivacyPolicy(user.getAcceptedPrivacyPolicy());
        dto.setVerify(user.getVerify());
        dto.setImage(user.getImage());

        // List emergencyContacts con el mapper
        Set<EmergencyContactDto> contacts = new HashSet<>();
        if (user.getEmergencyContacts() != null) {
            for (EmergencyContact contact : user.getEmergencyContacts()) {
                // Convertimos el EmergencyContact a EmergencyContactDto con el mapper
                EmergencyContactDto emergencyContactDto = EmergencyContactMapper.toDto(contact, dto);

                contacts.add(emergencyContactDto);
            }
        }
        dto.setEmergencyContacts(contacts);

        // List safeLocations con el mapper
        Set<SafeLocationDto> safeLocations = new HashSet<>();
        if (user.getSafeLocations() != null) {
            for (SafeLocation safeLocation : user.getSafeLocations()) {
                // Convertimos el SafeLocation a SafeLocationDto con el mapper
                SafeLocationDto safeLocationDto = SafeLocationMapper.toDto(safeLocation, dto);

                safeLocations.add(safeLocationDto);
            }
        }
        dto.setSafeLocations(safeLocations);

        return dto;
    }
}
