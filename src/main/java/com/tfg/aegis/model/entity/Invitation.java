package com.tfg.aegis.model.entity;

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

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "code_iv", length = 16)
    private byte[] codeIv;

    @Lob
    @Column(name = "code_ciphertext")
    private byte[] codeCiphertext;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
