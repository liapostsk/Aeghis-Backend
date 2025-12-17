package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Participation;
import com.tfg.aegis.model.dto.ParticipationDto;

public interface ParticipationMapper {
    Participation toEntity(ParticipationDto participationDto);

    ParticipationDto toDto(Participation participation);
}
