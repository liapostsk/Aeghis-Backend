package com.tfg.aegis.participacion.model;

import com.tfg.aegis.trayecto.model.Trayecto;
import com.tfg.aegis.ubicacion.model.Ubicacion;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.valoracion.model.Valoracion;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participaciones")
public class Participacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trayecto_id", nullable = false)
    private Trayecto trayecto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private User persona;

    @Column(nullable = false)
    private Boolean compartirUbi;

    // Ubicaciones asociadas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultima_ubicacion_id")
    private Ubicacion ultimaUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_id")
    private Ubicacion origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id")
    private Ubicacion destino;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.EstadoParticipacion estado;

    private LocalDateTime horaLlegada; // [0..1]

    // 0..1 Valoración
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "valoracion_id", unique = true)
    private Valoracion valoracion;
}