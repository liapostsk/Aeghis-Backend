package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Journey;
import com.tfg.aegis.model.dto.JourneyDto;

public interface JourneyMapper {
    Journey toEntity(JourneyDto journeyDto);

    JourneyDto toDto(Journey journey);
}
