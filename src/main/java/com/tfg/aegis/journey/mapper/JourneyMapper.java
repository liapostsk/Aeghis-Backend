package com.tfg.aegis.journey.mapper;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;

public interface JourneyMapper {
    Journey toEntity(JourneyDto journeyDto);

    JourneyDto toDto(Journey journey);
}
