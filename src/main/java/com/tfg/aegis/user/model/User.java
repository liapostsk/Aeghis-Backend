package com.tfg.aegis.user.model;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.externalcontact.model.ExternalContact;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.person.entity.Person;
import com.tfg.aegis.safelocation.model.SafeLocation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends Person {

    private String image;

    @Column(name = "accepted_privacy_policy", nullable = false)
    private Boolean acceptedPrivacyPolicy;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmergencyContact> emergencyContacts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExternalContact> externalContacts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SafeLocation> safeLocations = new HashSet<>();

    private Boolean verify;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

}