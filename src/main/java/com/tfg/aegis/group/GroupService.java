package com.tfg.aegis.group;

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
     * Method that allows a user to exit a group
     * @param groupId Group id
     * @param userId User id
     */
    void exitGroup(Long groupId, Long userId);

}