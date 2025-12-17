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
    private GroupState state; // Estado del grupo (e.g., "activo", "inactivo")

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expirationDate; // Fecha de expiración del grupo, si aplica
    private LocalDateTime lastModified; // Fecha de la última modificación del grupo

    // Propietario (transferible)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Miembros del grupo
    @ManyToMany
    @JoinTable(
            name = "group_user",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // Administradores (subset de users)
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
        // Asegura coherencia inicial
        if (owner != null) {
            members.add(owner);
            admins.add(owner);
        }
    }
}
