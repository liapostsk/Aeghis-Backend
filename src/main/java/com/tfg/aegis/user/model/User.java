package com.tfg.aegis.user.model;

import com.tfg.aegis.model.Person;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User extends Person {

    @Column(name = "image")
    private String image;

    @Column(name = "verify", nullable = false)
    private Boolean verify;

}