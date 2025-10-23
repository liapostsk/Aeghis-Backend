package com.tfg.aegis.location.mapper;

import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.location.model.LocationDto;

public interface LocationMapper {

    Location toEntity(LocationDto locationDto);

    LocationDto toDto(Location location);
}
