package com.tfg.aegis.safelocation.mapper;

import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;

public interface SafeLocationMapper {

    SafeLocation toEntity(SafeLocationDto dto, User user);

    SafeLocationDto toDto(SafeLocation safeLocation, UserDto userDto);
}
