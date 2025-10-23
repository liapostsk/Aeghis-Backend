package com.tfg.aegis.participacion.mapper;

import com.tfg.aegis.participacion.model.Participation;
import com.tfg.aegis.participacion.model.ParticipationDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ParticipationMapperImpl implements ParticipationMapper {

    @Override
    public Participation toEntity(ParticipationDto participationDto) {
        if ( participationDto == null ) {
            return null;
        }

        Participation participation = new Participation();

        return participation;
    }

    @Override
    public ParticipationDto toDto(Participation participation) {
        if (participation == null) {
            return null;
        }
        ParticipationDto dto = new ParticipationDto();
        return dto;
    }
}
