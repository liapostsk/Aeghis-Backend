package com.tfg.aegis.model.dto;

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
