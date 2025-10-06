package com.tfg.aegis.invitation.mapper;

import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapperImpl implements InvitationMapper {

    @Override
    public  InvitationDto toDto(Invitation invitation, String code) {
        if (invitation == null) return null;
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setGroupId(invitation.getGroup().getId());
        dto.setCode(code);
        dto.setExpiresAt(invitation.getExpiresAt());
        dto.setRevokedAt(invitation.getRevokedAt());
        dto.setCreatedAt(invitation.getCreatedAt());
        return dto;
    }

    @Override
    public Invitation toEntity(InvitationDto dto) {
        if (dto == null) return null;
        Invitation invitation = new Invitation();
        invitation.setId(dto.getId());
        invitation.setExpiresAt(dto.getExpiresAt());
        invitation.setRevokedAt(dto.getRevokedAt());
        invitation.setCreatedAt(dto.getCreatedAt());
        return invitation;
    }
}
