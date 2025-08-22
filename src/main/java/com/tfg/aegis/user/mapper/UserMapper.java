package com.tfg.aegis.user.mapper;

import com.tfg.aegis.user.model.UserDto;
import com.tfg.aegis.user.model.User;

public interface UserMapper {

    User toEntity(UserDto dto);

    UserDto toDto(User user);
}
