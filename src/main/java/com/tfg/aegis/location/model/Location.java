package com.tfg.aegis.location.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "locations")
@Inheritance(strategy = InheritanceType.JOINED)
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String name;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}