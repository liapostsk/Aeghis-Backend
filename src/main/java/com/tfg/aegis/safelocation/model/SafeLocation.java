package com.tfg.aegis.safelocation.model;

import com.tfg.aegis.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "safe_location")
public class SafeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double latitude;

    private Double longitude;

    private String address; // opcional: para guardar la dirección del lugar

    private String distance; // opcional: para guardar la distancia al lugar

    private String type; // e.g., "home", "bar", "safe_spot", etc.

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
