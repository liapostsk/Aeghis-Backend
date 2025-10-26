package com.tfg.aegis.group.mapper;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.person.user.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GroupMapperImpl implements GroupMapper {

    @Override
    public Group toEntity(GroupDto dto) {
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
        return group;
    }

    @Override
    public GroupDto toDto(Group group) {
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
        // Map members and admins to their IDs
        dto.setMembersIds(group.getMembers() != null
                ? group.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet())
                : java.util.Collections.emptySet());
        dto.setAdminsIds(group.getAdmins() != null
                ? group.getAdmins().stream()
                .map(User::getId)
                .collect(Collectors.toSet())
                : java.util.Collections.emptySet());
        return dto;
    }
}
