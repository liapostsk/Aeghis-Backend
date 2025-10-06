package com.tfg.aegis.invitation.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InvitationDto {
    private Long id;
    private Long groupId;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
}
