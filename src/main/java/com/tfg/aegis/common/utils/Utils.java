package com.tfg.aegis.common.utils;

import com.tfg.aegis.person.user.UserService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Utils {

    private final UserService userService;
//
//    public static UserDto getCurrentUser() {
//        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return userService.getUserByClerkId(clerkId);
//    }
}
