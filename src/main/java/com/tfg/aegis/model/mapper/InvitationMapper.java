package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Invitation;
import com.tfg.aegis.model.dto.InvitationDto;

public interface InvitationMapper {

    InvitationDto toDto(Invitation invitation, String code);

    Invitation toEntity(InvitationDto dto);
}
