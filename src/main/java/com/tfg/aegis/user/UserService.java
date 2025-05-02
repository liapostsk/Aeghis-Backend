package com.tfg.aegis.user;

import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;

public interface UserService {

    /**
     * Method that gets a User
     * @param id User id
     * @return UserDto
     */
    User getUser(Long id);

    /**
     * Method that creates a User
     * @body UserDto
     */
    Long createUser(UserDto userDto);

    /**
     * Method that updates the info of a User
     * @param id User id
     * @param userDto UserDto
     */
    void updateUser(Long id, UserDto userDto);

    /**
     * Method that deletes a User
     * @param id User id
     */
    void deleteUser(Long id);
}
