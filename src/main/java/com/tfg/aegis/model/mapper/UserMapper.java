package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.entity.User;

public interface UserMapper {

    User toEntity(UserDto dto);

    UserDto toDto(User user);
}
