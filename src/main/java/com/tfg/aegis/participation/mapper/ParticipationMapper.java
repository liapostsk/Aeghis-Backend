package com.tfg.aegis.participation.mapper;

import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.participation.model.ParticipationDto;

public interface ParticipationMapper {
    Participation toEntity(ParticipationDto participationDto);

    ParticipationDto toDto(Participation participation);
}
