package com.tfg.aegis.mapper;

import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;

import java.time.LocalDateTime;

public class InvitationMapper {
    public static InvitationDto toDto(Invitation invitation, String code) {
        if (invitation == null) return null;
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setGroup(invitation.getGroup());
        dto.setCode(code);
        dto.setExpiresAt(invitation.getExpiresAt());
        dto.setRevokedAt(invitation.getRevokedAt());
        dto.setCreatedAt(invitation.getCreatedAt());
        return dto;
    }

    public static Invitation toEntity(InvitationDto dto) {
        if (dto == null) return null;
        Invitation invitation = new Invitation();
        invitation.setId(dto.getId());
        invitation.setGroup(dto.getGroup());
        invitation.setExpiresAt(dto.getExpiresAt());
        invitation.setRevokedAt(dto.getRevokedAt());
        invitation.setCreatedAt(dto.getCreatedAt());
        return invitation;
    }
}
