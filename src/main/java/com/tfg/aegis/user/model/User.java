package com.tfg.aegis.user.model;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.externalcontact.model.ExternalContact;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.participacion.model.Participation;
import com.tfg.aegis.safelocation.model.SafeLocation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "clerkId", nullable = false)
    private String clerkId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "dateOfBirth", nullable = false)
    private Date dateOfBirth;

    private String image;

    @Column(name = "accepted_privacy_policy", nullable = false)
    private Boolean acceptedPrivacyPolicy;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Enums.TypeRole role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmergencyContact> emergencyContacts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExternalContact> externalContacts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SafeLocation> safeLocations = new HashSet<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participations = new HashSet<>();

    private Boolean verify;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

}