package com.tfg.aegis.person.user.mapper;

import com.tfg.aegis.person.user.model.UserDto;
import com.tfg.aegis.person.user.model.User;

public interface UserMapper {

    User toEntity(UserDto dto);

    UserDto toDto(User user);
}
