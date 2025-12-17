package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Location;
import com.tfg.aegis.model.dto.LocationDto;

public interface LocationMapper {

    Location toEntity(LocationDto locationDto);

    LocationDto toDto(Location location);
}
