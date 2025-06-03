package com.tfg.aegis.mapper;

import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;

public class SafeLocationMapper {

    private SafeLocationMapper() {
        // Previene instanciación
    }

    public static SafeLocation toEntity(SafeLocationDto dto, User user) {
        SafeLocation safeLocation = new SafeLocation();

        safeLocation.setName(dto.getName());
        safeLocation.setDescription(dto.getDescription());
        safeLocation.setLatitude(dto.getLatitude());
        safeLocation.setLongitude(dto.getLongitude());
        safeLocation.setExternalId(dto.getExternalId());
        safeLocation.setAddress(dto.getAddress());
        safeLocation.setDistance(dto.getDistance());
        safeLocation.setType(dto.getType());
        safeLocation.setOwner(user);

        return safeLocation;
    }

    public static SafeLocationDto toDto(SafeLocation safeLocation, UserDto userDto) {
        SafeLocationDto dto = new SafeLocationDto();

        dto.setName(safeLocation.getName());
        dto.setDescription(safeLocation.getDescription());
        dto.setLatitude(safeLocation.getLatitude());
        dto.setLongitude(safeLocation.getLongitude());
        dto.setExternalId(safeLocation.getExternalId());
        dto.setType(safeLocation.getType());
        dto.setUserId(userDto.getId());

        return dto;
    }
}
