package com.tfg.aegis.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "safe_location")
public class SafeLocation extends Location {

    @Column(unique = true)
    private String externalId;

    private String description;

    private String address;

    private String distance;

    private String type;

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

}
