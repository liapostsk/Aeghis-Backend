package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.UserEnums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User extends Person {

    @Column(name = "clerkId", nullable = false)
    private String clerkId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "dateOfBirth", nullable = false)
    private Date dateOfBirth;

    private String image;

    @Column(name = "accepted_privacy_policy", nullable = false)
    private Boolean acceptedPrivacyPolicy;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserEnums.TypeRole role;

    @Enumerated(EnumType.STRING)
    private UserEnums.VerificationStatus verify;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmergencyContact> emergencyContacts = new HashSet<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmergencyContact> emergencyContactsAsContact = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExternalContact> externalContacts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SafeLocation> safeLocations = new HashSet<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participation = new HashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationToken> notificationTokens = new HashSet<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<CompanionRequest> companionRequestsCreated = new HashSet<>();

    @OneToMany(mappedBy = "companion", cascade = CascadeType.REMOVE)
    private Set<CompanionRequest> companionRequestsAccepted = new HashSet<>();

}