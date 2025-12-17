package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.ParticipationEnums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "participants", uniqueConstraints = @UniqueConstraint(columnNames = {"journey_id", "user_id"})
)
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;

    @Column(nullable = false)
    private Boolean sharedLocation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "participation_id")
    @OrderBy("timestamp ASC")
    private Set<Location> positions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Location source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Location destination;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ParticipationEnums.ParticipationState state;

    private LocalDateTime arrivalTime; // [0..1]

    // 0..1 Valoración
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "valoracion_id", unique = true)
    private Valoracion valoracion;
}