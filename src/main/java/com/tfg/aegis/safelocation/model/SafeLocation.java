package com.tfg.aegis.safelocation.model;

import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.person.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "safe_location")
public class SafeLocation extends Location {

    @Column(unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String description;

    private String address; // opcional: para guardar la dirección del lugar

    private String distance; // opcional: para guardar la distancia al lugar

    private String type; // e.g., "home", "bar", "safe_spot", etc.

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    private LocalDateTime createdAt = LocalDateTime.now();
}
