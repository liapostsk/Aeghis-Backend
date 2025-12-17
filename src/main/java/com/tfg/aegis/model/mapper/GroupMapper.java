package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.GroupDto;

public interface GroupMapper {

    Group toEntity(GroupDto dto);

    GroupDto toDto(Group group);
}
