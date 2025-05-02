package com.tfg.aegis.user.model;

import com.tfg.aegis.model.PersonDto;
import lombok.Data;

@Data
public class UserDto extends PersonDto {
    private String image;
    private Boolean verify;
    // private List<UserDto> emergencyContacts; (si lo usas luego)
}
