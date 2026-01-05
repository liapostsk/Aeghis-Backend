package com.tfg.aegis.service;

import com.tfg.aegis.model.mapper.LocationMapper;
import com.tfg.aegis.model.entity.Location;
import com.tfg.aegis.model.dto.LocationDto;
import com.tfg.aegis.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    /**
     * Retrieves a location entity by its ID.
     *
     * @param id The ID of the location entity.
     * @return The location entity with the specified ID.
     */
    public LocationDto getLocation(Long id) {
        return locationRepository.findById(id)
                .map(locationMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Location with id %s not found".formatted(id)));
    }

    /**
     * Saves a location entity to the database.
     *
     * @param locationDto The location entity to be saved.
     * @return The saved location entity.
     */
    public Long createLocation(LocationDto locationDto) {
        // Map LocationDto to Location entity
        Location location = locationMapper.toEntity(locationDto);

        locationRepository.save(location);

        return location.getId();
    }

    /**
     * Deletes a location entity from the database.
     *
     * @param id The location entity to be deleted.
     */
    public void deleteLocation(Long id) {
        LocationDto locationDto = getLocation(id);
        Location location = locationMapper.toEntity(locationDto);
        locationRepository.delete(location);
    }
}
