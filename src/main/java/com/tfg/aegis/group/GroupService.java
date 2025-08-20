package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.GroupDto;

import java.util.List;

public interface GroupService {
    /**
     * Method that creates a Group
     * @param groupDto GroupDto
     * @return Group id
     */
    Long createGroup(GroupDto groupDto);

    /**
     * Method that allows a user to join a group
     * @param groupId Group id
     * @param userId User id
     */
    void joinGroup(Long groupId, Long userId, String code);

    /**
     * Method that retrieves all groups of a specific type
     * @param type Type of group
     * @return List of GroupDto
     */
    List<GroupDto> getAllGroupsByType(Enums.TypeGroup type);

    /**
     * Method that allows a user to exit a group
     * @param groupId Group id
     * @param userId User id
     */
    void exitGroup(Long groupId, Long userId);

}