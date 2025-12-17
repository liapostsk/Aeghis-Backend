package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.JourneyEnums;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "trayectos")
public class Journey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private JourneyEnums.JourneyState state;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private JourneyEnums.JourneyType journeyType;

    @Column(nullable = false) private LocalDateTime iniDate;

    private LocalDateTime endDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participations = new HashSet<>();

    @OneToOne(mappedBy = "trayecto")
    private CompanionRequest companionRequest;
}
