package com.tfg.aegis.journey.model;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.location.model.Location;
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
    private Enums.EstadoTrayecto state;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.TipoTrayecto journeyType;

    @Column(nullable = false) private LocalDateTime iniDate;
    @Column(nullable = false) private LocalDateTime endDate;

    // Puntos de origen y destino
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Location sourcePoint;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", nullable = false)
    private Location destino;

    //grupo asociado
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Group group;

    // 1..* Participaciones
    @OneToMany(mappedBy = "participations", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participations = new HashSet<>();
}
