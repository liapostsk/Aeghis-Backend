package com.tfg.aegis.location;

import com.tfg.aegis.location.model.LocationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Location", description = "API of locations")
@RestController
@AllArgsConstructor
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    @Operation(summary = "Get", description = "Method that gets a Location")
    @GetMapping(path = "/{id}")
    public ResponseEntity<LocationDto> getLocation(@PathVariable(name = "id") Long id) {
        LocationDto locationDto = locationService.getLocation(id);
        return ResponseEntity.ok(locationDto);
    }

    @Operation(summary = "Save", description = "Method that saves a Location")
    @PostMapping
    public ResponseEntity<Void> saveLocation(@RequestBody LocationDto locationDto) {
        locationService.saveLocation(locationDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete", description = "Method that deletes a Location")
    @DeleteMapping
    public ResponseEntity<Void> deleteLocation(@RequestBody LocationDto locationDto) {
        locationService.deleteLocation(locationDto);
        return ResponseEntity.noContent().build();
    }
}
