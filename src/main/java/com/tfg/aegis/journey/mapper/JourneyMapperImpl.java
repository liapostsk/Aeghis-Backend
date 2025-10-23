package com.tfg.aegis.journey.mapper;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JourneyMapperImpl implements JourneyMapper {

    @Override
    public Journey toEntity(JourneyDto journeyDto) {
        if ( journeyDto == null ) {
            return null;
        }

        Journey journey = new Journey();

        journey.setId(journeyDto.getId());
        journey.setFechaInicio(journeyDto.getFechaInicio());
        journey.setFechaFin(journeyDto.getFechaFin());
        journey.setEstado(journeyDto.getEstado());

        return journey;
    }

    @Override
    public JourneyDto toDto(Journey journey) {
        if (journey == null) {
            return null;
        }
        JourneyDto journeyDto = new JourneyDto();
        journeyDto.setId(journey.getId());
        journeyDto.setFechaInicio(journey.getFechaInicio());
        journeyDto.setFechaFin(journey.getFechaFin());
        journeyDto.setEstado(journey.getEstado());
        return journeyDto;
    }
}
