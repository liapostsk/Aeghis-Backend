package com.tfg.aegis.location.mapper;

import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.location.model.LocationDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LocationMapperImpl implements LocationMapper {

    @Override
    public Location toEntity(LocationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Location location = new Location();

        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setTimestamp(dto.getTimestamp());
        location.setName(dto.getName());

        return location;
    }

    @Override
    public LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setLongitude(location.getLongitude());
        dto.setLatitude(location.getLatitude());
        dto.setTimestamp(location.getTimestamp());
        dto.setName(location.getName());
        return dto;
    }

}
