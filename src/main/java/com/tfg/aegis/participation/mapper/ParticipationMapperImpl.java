package com.tfg.aegis.participation.mapper;

import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.participation.model.ParticipationDto;
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
        participation.setId(participation.getId());
        participation.setState(participationDto.getState());
        participation.setSharedLocation(participationDto.getSharedLocation());
        participation.setArrivalTime(participationDto.getArrivalTime());

        return participation;
    }

    @Override
    public ParticipationDto toDto(Participation participation) {
        if (participation == null) {
            return null;
        }
        ParticipationDto dto = new ParticipationDto();
        dto.setId(participation.getId());
        dto.setState(participation.getState());
        dto.setSharedLocation(participation.getSharedLocation());
        dto.setArrivalTime(participation.getArrivalTime());

        return dto;
    }
}
