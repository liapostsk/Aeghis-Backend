package com.tfg.aegis.service;

import com.tfg.aegis.model.dto.LocationDto;
import com.tfg.aegis.model.entity.Location;
import com.tfg.aegis.model.mapper.LocationMapper;
import com.tfg.aegis.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService service;

    /* =========================
     * getLocation
     * ========================= */
    @Test
    void getLocation_whenExists_returnsDto() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);
        location.setLatitude(41.3874);
        location.setLongitude(2.1686);

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setLatitude(41.3874);
        dto.setLongitude(2.1686);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        LocationDto result = service.getLocation(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(41.3874, result.getLatitude());
        assertEquals(2.1686, result.getLongitude());
        verify(locationRepository).findById(id);
        verify(locationMapper).toDto(location);
    }

    @Test
    void getLocation_whenNotExists_throwsRuntimeException() {
        Long id = 999L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getLocation(id));

        assertTrue(ex.getMessage().contains("Location with id"));
        assertTrue(ex.getMessage().contains("999"));
        assertTrue(ex.getMessage().contains("not found"));
        verify(locationMapper, never()).toDto(any());
    }

    /* =========================
     * createLocation
     * ========================= */
    @Test
    void createLocation_ok_savesAndReturnsId() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(40.4168);
        dto.setLongitude(-3.7038);
        dto.setName("Madrid");

        Location entity = new Location();
        entity.setLatitude(40.4168);
        entity.setLongitude(-3.7038);
        entity.setName("Madrid");

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(100L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(100L, result);
        verify(locationMapper).toEntity(dto);
        verify(locationRepository).save(entity);
    }

    @Test
    void createLocation_withAllFields_savesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(51.5074);
        dto.setLongitude(-0.1278);
        dto.setName("London");

        Location entity = new Location();
        entity.setLatitude(51.5074);
        entity.setLongitude(-0.1278);
        entity.setName("London");

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(captor.capture())).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(200L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(200L, result);
        Location saved = captor.getValue();
        assertEquals(51.5074, saved.getLatitude());
        assertEquals(-0.1278, saved.getLongitude());
        assertEquals("London", saved.getName());
    }

    @Test
    void createLocation_withMinimalFields_savesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(0.0);
        dto.setLongitude(0.0);

        Location entity = new Location();
        entity.setLatitude(0.0);
        entity.setLongitude(0.0);

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(300L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(300L, result);
        verify(locationRepository).save(entity);
    }

    /* =========================
     * deleteLocation
     * ========================= */
    @Test
    void deleteLocation_ok_deletesEntity() {
        LocationDto dto = new LocationDto();
        dto.setId(10L);
        dto.setLatitude(48.8566);
        dto.setLongitude(2.3522);

        Location entity = new Location();
        entity.setId(10L);
        entity.setLatitude(48.8566);
        entity.setLongitude(2.3522);

        when(locationMapper.toEntity(dto)).thenReturn(entity);

        service.deleteLocation(dto.getId());

        verify(locationMapper).toEntity(dto);
        verify(locationRepository).delete(entity);
    }

    @Test
    void deleteLocation_withAllFields_deletesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setId(20L);
        dto.setLatitude(35.6762);
        dto.setLongitude(139.6503);
        dto.setName("Tokyo");

        Location entity = new Location();
        entity.setId(20L);
        entity.setLatitude(35.6762);
        entity.setLongitude(139.6503);
        entity.setName("Tokyo");

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);

        when(locationMapper.toEntity(dto)).thenReturn(entity);

        service.deleteLocation(dto.getId());

        verify(locationRepository).delete(captor.capture());
        Location deleted = captor.getValue();
        assertEquals(20L, deleted.getId());
        assertEquals(35.6762, deleted.getLatitude());
        assertEquals(139.6503, deleted.getLongitude());
        assertEquals("Tokyo", deleted.getName());
    }

    @Test
    void deleteLocation_callsMapperAndRepository() {
        LocationDto dto = new LocationDto();
        dto.setId(30L);

        Location entity = new Location();
        entity.setId(30L);

        when(locationMapper.toEntity(dto)).thenReturn(entity);

        service.deleteLocation(dto.getId());

        verify(locationMapper).toEntity(dto);
        verify(locationRepository).delete(entity);
        verifyNoMoreInteractions(locationMapper, locationRepository);
    }

    @Test
    void createAndGetLocation_integration() {
        // Crear
        LocationDto createDto = new LocationDto();
        createDto.setLatitude(37.7749);
        createDto.setLongitude(-122.4194);
        createDto.setName("San Francisco");

        Location entity = new Location();
        entity.setLatitude(37.7749);
        entity.setLongitude(-122.4194);
        entity.setName("San Francisco");

        when(locationMapper.toEntity(createDto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(500L);
            return loc;
        });

        Long createdId = service.createLocation(createDto);
        assertEquals(500L, createdId);

        // Obtener - usar el mismo objeto que se guardó
        LocationDto getDto = new LocationDto();
        getDto.setId(500L);
        getDto.setLatitude(37.7749);
        getDto.setLongitude(-122.4194);

        when(locationRepository.findById(500L)).thenReturn(Optional.of(entity));
        when(locationMapper.toDto(entity)).thenReturn(getDto);

        LocationDto retrieved = service.getLocation(createdId);

        assertEquals(createdId, retrieved.getId());
        assertEquals(37.7749, retrieved.getLatitude());
        assertEquals(-122.4194, retrieved.getLongitude());
    }

    @Test
    void createLocation_negativeCoordinates_handlesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(-33.8688);
        dto.setLongitude(151.2093);
        dto.setName("Sydney");

        Location entity = new Location();
        entity.setLatitude(-33.8688);
        entity.setLongitude(151.2093);
        entity.setName("Sydney");

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(600L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(600L, result);
        verify(locationRepository).save(entity);
    }

    @Test
    void getLocation_multipleCallsSameId_callsRepositoryEachTime() {
        Long id = 1L;
        Location location = new Location();
        location.setId(id);

        LocationDto dto = new LocationDto();
        dto.setId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        service.getLocation(id);
        service.getLocation(id);
        service.getLocation(id);

        verify(locationRepository, times(3)).findById(id);
        verify(locationMapper, times(3)).toDto(location);
    }
}
