package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "valoraciones")
public class Valoracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Enums.TipoValoracion valor;

    @Column(length = 500) private String comentario; // [0..1] opcional
    @Column(nullable = false) private LocalDateTime fecha;

    @Column(name = "participation_id", nullable = false)
    private Long participationId;
}