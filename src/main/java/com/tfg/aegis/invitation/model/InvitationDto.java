package com.tfg.aegis.invitation.model;

import java.time.LocalDateTime;

import com.tfg.aegis.group.model.Group;

import lombok.Data;

@Data
public class InvitationDto {
    private Long id;
    private Group group;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
}
