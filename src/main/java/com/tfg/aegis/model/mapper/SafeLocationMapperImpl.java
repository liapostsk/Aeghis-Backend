package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class SafeLocationMapperImpl implements SafeLocationMapper {

    @Override
    public SafeLocation toEntity(SafeLocationDto dto, User user) {
        SafeLocation safeLocation = new SafeLocation();

        safeLocation.setId(dto.getId());
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

    @Override
    public SafeLocationDto toDto(SafeLocation safeLocation, UserDto userDto) {
        SafeLocationDto dto = new SafeLocationDto();

        dto.setId(safeLocation.getId());
        dto.setName(safeLocation.getName());
        dto.setDescription(safeLocation.getDescription());
        dto.setLatitude(safeLocation.getLatitude());
        dto.setLongitude(safeLocation.getLongitude());
        dto.setExternalId(safeLocation.getExternalId());
        dto.setAddress(safeLocation.getAddress());
        dto.setDistance(safeLocation.getDistance());
        dto.setType(safeLocation.getType());
        dto.setUserId(userDto.getId());

        return dto;
    }
}
