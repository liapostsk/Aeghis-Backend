package com.tfg.aegis.participation.model;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.valoracion.model.Valoracion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "participations")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trayecto_id", nullable = false)
    private Journey journey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private User persona;

    @Column(nullable = false)
    private Boolean shareLocation;

    // Ubicaciones asociadas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_location_id")
    private Location lastLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Location source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Location destination;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.EstadoParticipacion state;

    private LocalDateTime arrivalTime; // [0..1]

    // 0..1 Valoración
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "valoracion_id", unique = true)
    private Valoracion valoracion;
}