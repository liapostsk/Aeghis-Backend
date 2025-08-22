package com.tfg.aegis.invitation.mapper;

import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;

public interface InvitationMapper {

    InvitationDto toDto(Invitation invitation, String code);

    Invitation toEntity(InvitationDto dto);
}
