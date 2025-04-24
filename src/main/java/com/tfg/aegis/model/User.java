package com.tfg.aegis.model;

import lombok.Data;

import java.util.List;

@Data
public class User extends Person{
    String image;
    Boolean valid;
    String genre;
    //List<User> emergencyContacts;
}
