package com.tfg.aegis.group.mapper;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.user.model.User;

public interface GroupMapper {

    Group toEntity(GroupDto dto, User owner);

    GroupDto toDto(Group group);
}
