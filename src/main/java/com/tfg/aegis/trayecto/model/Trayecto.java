package com.tfg.aegis.trayecto.model;

import com.tfg.aegis.participacion.model.Participacion;
import com.tfg.aegis.ubicacion.model.Ubicacion;
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
public class Trayecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.EstadoTrayecto estado;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.TipoTrayecto tipoTrayecto;

    @Column(nullable = false) private LocalDateTime fechaInicio;
    @Column(nullable = false) private LocalDateTime fechaFin;

    // Puntos de salida/llegada
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_point_id", nullable = false)
    private Ubicacion sourcePoint;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", nullable = false)
    private Ubicacion destino;

    // 1..* Participaciones
    @OneToMany(mappedBy = "trayecto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participacion> participations = new HashSet<>();
}
