package com.tfg.aegis.group.mapper;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;

public interface GroupMapper {

    Group toEntity(GroupDto dto);

    GroupDto toDto(Group group);
}
