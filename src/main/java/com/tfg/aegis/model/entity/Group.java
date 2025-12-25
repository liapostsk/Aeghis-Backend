package com.tfg.aegis.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.tfg.aegis.model.enums.GroupEnums.*;

@Entity
@Getter
@Setter
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeGroup type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupState state;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expirationDate;
    private LocalDateTime lastModified;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "group_user",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "group_admin",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Journey> journeys = new HashSet<>();

    @OneToOne(mappedBy = "companionGroup")
    private CompanionRequest companionRequest;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (owner != null) {
            members.add(owner);
            admins.add(owner);
        }
    }
}
