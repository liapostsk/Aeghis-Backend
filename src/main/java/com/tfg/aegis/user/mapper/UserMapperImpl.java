package com.tfg.aegis.user.mapper;

import com.tfg.aegis.safelocation.mapper.SafeLocationMapperImpl;
import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import com.tfg.aegis.user.model.UserDto;
import com.tfg.aegis.user.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

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

        // List safeLocations con el mapper
        Set<SafeLocationDto> safeLocations = new HashSet<>();
        if (user.getSafeLocations() != null) {
            for (SafeLocation safeLocation : user.getSafeLocations()) {
                // Convertimos el SafeLocation a SafeLocationDto con el mapper
                SafeLocationDto safeLocationDto = mapper.toDto(safeLocation, dto);

                safeLocations.add(safeLocationDto);
            }
        }
        dto.setSafeLocations(safeLocations);

        return dto;
    }
}
