package com.tfg.aegis.common.utils;

import com.tfg.aegis.person.user.UserService;
import com.tfg.aegis.person.user.model.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

@AllArgsConstructor
public class Utils {

    private static UserService userService;

    public static UserDto getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByClerkId(clerkId);
    }
}
