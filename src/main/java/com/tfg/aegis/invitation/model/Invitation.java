package com.tfg.aegis.invitation.model;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "invitation")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    // Código en texto plano NO se debería guardar, mejor el hash
    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    // Fecha de caducidad
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Fecha de revocación (null si sigue activa)
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
