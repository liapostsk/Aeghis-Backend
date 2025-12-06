package com.tfg.aegis.journey.model;

import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.participation.model.Participation;

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
    private Enums.JourneyState state;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.JourneyType journeyType;

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
