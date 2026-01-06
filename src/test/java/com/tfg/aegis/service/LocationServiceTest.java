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
        Long id = 10L;

        // Mock para getLocation(id)
        Location locationFromDb = new Location();
        locationFromDb.setId(id);
        locationFromDb.setLatitude(48.8566);
        locationFromDb.setLongitude(2.3522);

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setLatitude(48.8566);
        dto.setLongitude(2.3522);

        // Mock para la conversión final a entity para delete
        Location entityToDelete = new Location();
        entityToDelete.setId(id);
        entityToDelete.setLatitude(48.8566);
        entityToDelete.setLongitude(2.3522);

        when(locationRepository.findById(id)).thenReturn(Optional.of(locationFromDb));
        when(locationMapper.toDto(locationFromDb)).thenReturn(dto);
        when(locationMapper.toEntity(dto)).thenReturn(entityToDelete);

        service.deleteLocation(id);

        verify(locationRepository).findById(id);
        verify(locationMapper).toDto(locationFromDb);
        verify(locationMapper).toEntity(dto);
        verify(locationRepository).delete(entityToDelete);
    }

    @Test
    void deleteLocation_withAllFields_deletesCorrectly() {
        Long id = 20L;

        Location locationFromDb = new Location();
        locationFromDb.setId(id);
        locationFromDb.setLatitude(35.6762);
        locationFromDb.setLongitude(139.6503);
        locationFromDb.setName("Tokyo");

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setLatitude(35.6762);
        dto.setLongitude(139.6503);
        dto.setName("Tokyo");

        Location entityToDelete = new Location();
        entityToDelete.setId(id);
        entityToDelete.setLatitude(35.6762);
        entityToDelete.setLongitude(139.6503);
        entityToDelete.setName("Tokyo");

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);

        when(locationRepository.findById(id)).thenReturn(Optional.of(locationFromDb));
        when(locationMapper.toDto(locationFromDb)).thenReturn(dto);
        when(locationMapper.toEntity(dto)).thenReturn(entityToDelete);

        service.deleteLocation(id);

        verify(locationRepository).delete(captor.capture());
        Location deleted = captor.getValue();
        assertEquals(20L, deleted.getId());
        assertEquals(35.6762, deleted.getLatitude());
        assertEquals(139.6503, deleted.getLongitude());
        assertEquals("Tokyo", deleted.getName());
    }

    @Test
    void deleteLocation_callsMapperAndRepository() {
        Long id = 30L;

        Location locationFromDb = new Location();
        locationFromDb.setId(id);

        LocationDto dto = new LocationDto();
        dto.setId(id);

        Location entityToDelete = new Location();
        entityToDelete.setId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(locationFromDb));
        when(locationMapper.toDto(locationFromDb)).thenReturn(dto);
        when(locationMapper.toEntity(dto)).thenReturn(entityToDelete);

        service.deleteLocation(id);

        verify(locationRepository).findById(id);
        verify(locationMapper).toDto(locationFromDb);
        verify(locationMapper).toEntity(dto);
        verify(locationRepository).delete(entityToDelete);
    }

    @Test
    void deleteLocation_whenNotExists_throwsRuntimeException() {
        Long id = 999L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteLocation(id));

        assertTrue(ex.getMessage().contains("Location with id"));
        assertTrue(ex.getMessage().contains("999"));
        assertTrue(ex.getMessage().contains("not found"));
        verify(locationRepository).findById(id);
        verify(locationMapper, never()).toEntity(any());
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void deleteLocation_withZeroId_deletesCorrectly() {
        Long id = 0L;

        Location locationFromDb = new Location();
        locationFromDb.setId(id);

        LocationDto dto = new LocationDto();
        dto.setId(id);

        Location entityToDelete = new Location();
        entityToDelete.setId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(locationFromDb));
        when(locationMapper.toDto(locationFromDb)).thenReturn(dto);
        when(locationMapper.toEntity(dto)).thenReturn(entityToDelete);

        service.deleteLocation(id);

        verify(locationRepository).findById(id);
        verify(locationRepository).delete(entityToDelete);
    }

    @Test
    void deleteLocation_withMaxLongId_deletesCorrectly() {
        Long id = Long.MAX_VALUE;

        Location locationFromDb = new Location();
        locationFromDb.setId(id);

        LocationDto dto = new LocationDto();
        dto.setId(id);

        Location entityToDelete = new Location();
        entityToDelete.setId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(locationFromDb));
        when(locationMapper.toDto(locationFromDb)).thenReturn(dto);
        when(locationMapper.toEntity(dto)).thenReturn(entityToDelete);

        service.deleteLocation(id);

        verify(locationRepository).findById(id);
        verify(locationRepository).delete(entityToDelete);
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

    @Test
    void getLocation_withZeroId_returnsDto() {
        Long id = 0L;
        Location location = new Location();
        location.setId(id);
        location.setLatitude(0.0);
        location.setLongitude(0.0);

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setLatitude(0.0);
        dto.setLongitude(0.0);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        LocationDto result = service.getLocation(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(locationRepository).findById(id);
    }

    @Test
    void getLocation_withMaxLongId_returnsDto() {
        Long id = Long.MAX_VALUE;
        Location location = new Location();
        location.setId(id);

        LocationDto dto = new LocationDto();
        dto.setId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        LocationDto result = service.getLocation(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(locationRepository).findById(id);
    }

    @Test
    void createLocation_multipleLocations_savesAll() {
        LocationDto dto1 = new LocationDto();
        dto1.setLatitude(1.0);
        dto1.setLongitude(1.0);

        LocationDto dto2 = new LocationDto();
        dto2.setLatitude(2.0);
        dto2.setLongitude(2.0);

        Location entity1 = new Location();
        entity1.setLatitude(1.0);
        entity1.setLongitude(1.0);

        Location entity2 = new Location();
        entity2.setLatitude(2.0);
        entity2.setLongitude(2.0);

        when(locationMapper.toEntity(dto1)).thenReturn(entity1);
        when(locationMapper.toEntity(dto2)).thenReturn(entity2);
        when(locationRepository.save(entity1)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(1L);
            return loc;
        });
        when(locationRepository.save(entity2)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(2L);
            return loc;
        });

        Long id1 = service.createLocation(dto1);
        Long id2 = service.createLocation(dto2);

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        verify(locationRepository).save(entity1);
        verify(locationRepository).save(entity2);
    }

    @Test
    void createLocation_extremeCoordinates_handlesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(90.0);  // North Pole
        dto.setLongitude(180.0); // Extreme longitude
        dto.setName("North Pole");

        Location entity = new Location();
        entity.setLatitude(90.0);
        entity.setLongitude(180.0);
        entity.setName("North Pole");

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(700L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(700L, result);
        verify(locationRepository).save(entity);
    }

    @Test
    void createLocation_withNullName_savesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(25.0);
        dto.setLongitude(55.0);
        dto.setName(null);

        Location entity = new Location();
        entity.setLatitude(25.0);
        entity.setLongitude(55.0);
        entity.setName(null);

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(800L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(800L, result);
        verify(locationRepository).save(entity);
    }

    @Test
    void createLocation_withEmptyName_savesCorrectly() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(30.0);
        dto.setLongitude(60.0);
        dto.setName("");

        Location entity = new Location();
        entity.setLatitude(30.0);
        entity.setLongitude(60.0);
        entity.setName("");

        when(locationMapper.toEntity(dto)).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(900L);
            return loc;
        });

        Long result = service.createLocation(dto);

        assertEquals(900L, result);
        verify(locationRepository).save(entity);
    }

    @Test
    void deleteLocation_multipleLocations_deletesAll() {
        Long id1 = 10L;
        Long id2 = 20L;

        // First deletion
        Location loc1FromDb = new Location();
        loc1FromDb.setId(id1);
        LocationDto dto1 = new LocationDto();
        dto1.setId(id1);
        Location entity1 = new Location();
        entity1.setId(id1);

        when(locationRepository.findById(id1)).thenReturn(Optional.of(loc1FromDb));
        when(locationMapper.toDto(loc1FromDb)).thenReturn(dto1);
        when(locationMapper.toEntity(dto1)).thenReturn(entity1);

        // Second deletion
        Location loc2FromDb = new Location();
        loc2FromDb.setId(id2);
        LocationDto dto2 = new LocationDto();
        dto2.setId(id2);
        Location entity2 = new Location();
        entity2.setId(id2);

        when(locationRepository.findById(id2)).thenReturn(Optional.of(loc2FromDb));
        when(locationMapper.toDto(loc2FromDb)).thenReturn(dto2);
        when(locationMapper.toEntity(dto2)).thenReturn(entity2);

        service.deleteLocation(id1);
        service.deleteLocation(id2);

        verify(locationRepository).delete(entity1);
        verify(locationRepository).delete(entity2);
    }

    @Test
    void getLocation_withName_includesNameInDto() {
        Long id = 5L;
        Location location = new Location();
        location.setId(id);
        location.setLatitude(45.0);
        location.setLongitude(90.0);
        location.setName("Test Location");

        LocationDto dto = new LocationDto();
        dto.setId(id);
        dto.setLatitude(45.0);
        dto.setLongitude(90.0);
        dto.setName("Test Location");

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        LocationDto result = service.getLocation(id);

        assertNotNull(result);
        assertEquals("Test Location", result.getName());
        verify(locationMapper).toDto(location);
    }

    @Test
    void createLocation_verifyMapperCalledWithCorrectDto() {
        LocationDto dto = new LocationDto();
        dto.setLatitude(12.345);
        dto.setLongitude(67.890);
        dto.setName("Specific Location");

        Location entity = new Location();
        entity.setLatitude(12.345);
        entity.setLongitude(67.890);
        entity.setName("Specific Location");

        ArgumentCaptor<LocationDto> captor = ArgumentCaptor.forClass(LocationDto.class);

        when(locationMapper.toEntity(captor.capture())).thenReturn(entity);
        when(locationRepository.save(entity)).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            loc.setId(1000L);
            return loc;
        });

        service.createLocation(dto);

        LocationDto captured = captor.getValue();
        assertEquals(12.345, captured.getLatitude());
        assertEquals(67.890, captured.getLongitude());
        assertEquals("Specific Location", captured.getName());
    }
}
