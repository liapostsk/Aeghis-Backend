package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;

public interface SafeLocationMapper {

    SafeLocation toEntity(SafeLocationDto dto, User user);

    SafeLocationDto toDto(SafeLocation safeLocation, UserDto userDto);
}
