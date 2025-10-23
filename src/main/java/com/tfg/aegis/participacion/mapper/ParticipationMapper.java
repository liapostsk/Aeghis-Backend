package com.tfg.aegis.participacion.mapper;

import com.tfg.aegis.participacion.model.Participation;
import com.tfg.aegis.participacion.model.ParticipationDto;

public interface ParticipationMapper {
    Participation toEntity(ParticipationDto participationDto);

    ParticipationDto toDto(Participation participation);
}
