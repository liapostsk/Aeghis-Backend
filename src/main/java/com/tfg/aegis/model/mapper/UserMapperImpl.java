package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final SafeLocationMapperImpl mapper;

    @Override
    public User toEntity(UserDto dto) {
        User user = new User();
        user.setClerkId(dto.getClerkId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setAcceptedPrivacyPolicy(dto.getAcceptedPrivacyPolicy());
        user.setVerify(dto.getVerify());
        user.setImage(dto.getImage());
        user.setRole(dto.getRole());
        user.setVerify(dto.getVerify());

        // List safeLocations con el mapper
        Set<SafeLocation> safeLocations = new HashSet<>();
        if (dto.getSafeLocations() != null) {
            for (SafeLocationDto safeLocationDto : dto.getSafeLocations()) {
                // Convertimos el SafeLocationDto a SafeLocation con el mapper
                SafeLocation safeLocation = mapper.toEntity(safeLocationDto, user);

                safeLocations.add(safeLocation);
            }
        }
        user.setSafeLocations(safeLocations);

        return user;
    }

    @Override
    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setClerkId(user.getClerkId());
        dto.setAcceptedPrivacyPolicy(user.getAcceptedPrivacyPolicy());
        dto.setVerify(user.getVerify());
        dto.setImage(user.getImage());
        dto.setRole(user.getRole());
        dto.setVerify(user.getVerify());
        dto.setCompanionRequestsCreatedIds(
                user.getCompanionRequestsCreated()
                        .stream().map(CompanionRequest::getId).collect(Collectors.toSet()));
        dto.setCompanionRequestsAcceptedIds(
                user.getCompanionRequestsAccepted()
                        .stream()
                        .map(CompanionRequest::getId)
                        .collect(Collectors.toSet()));
        dto.setSafeLocations(
                user.getSafeLocations()
                        .stream()
                        .map(safeLocation -> mapper.toDto(safeLocation, dto))
                        .collect(Collectors.toSet()));
        return dto;
    }
}