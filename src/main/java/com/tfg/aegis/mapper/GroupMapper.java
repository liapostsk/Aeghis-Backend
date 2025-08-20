package com.tfg.aegis.mapper;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.user.model.User;

public class GroupMapper {
    public static GroupDto toDto(Group group) {
        if (group == null) return null;
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setImageUrl(group.getImageUrl());
        dto.setType(group.getType());
        dto.setState(group.getState());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setExpirationDate(group.getExpirationDate());
        dto.setLastModified(group.getLastModified());
        dto.setOwnerId(group.getOwner() != null ? group.getOwner().getId() : null);
        return dto;
    }

    public static Group toEntity(GroupDto dto, User owner) {
        if (dto == null) return null;
        Group group = new Group();
        group.setId(dto.getId());
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setImageUrl(dto.getImageUrl());
        group.setType(dto.getType());
        group.setState(dto.getState());
        group.setCreatedAt(dto.getCreatedAt());
        group.setExpirationDate(dto.getExpirationDate());
        group.setLastModified(dto.getLastModified());
        group.setOwner(owner);
        return group;
    }
}
