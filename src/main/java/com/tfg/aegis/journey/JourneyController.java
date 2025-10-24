package com.tfg.aegis.journey;

import com.tfg.aegis.journey.model.JourneyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Journey", description = "API of Journeys")
@RequestMapping(value = "/journey")
@RestController
@AllArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    @Operation(summary = "Get Journey", description = "Method that gets a Journey")
    @GetMapping(path = "{id}")
    public ResponseEntity<JourneyDto> getJourney(@PathVariable(name = "id") Long id) {
        JourneyDto journeyDto = journeyService.getJourney(id);
        return ResponseEntity.ok(journeyDto);
    }

    @Operation(summary = "Get Current Journey", description = "Method that gets the current Journey for a user")
    @GetMapping(path = "/current/{userId}")
    public ResponseEntity<JourneyDto> getCurrentJourney(@PathVariable(name = "userId") Long userId) {
        JourneyDto journeyDto = journeyService.getCurrentJourney(userId);
        return ResponseEntity.ok(journeyDto);
    }

    @Operation(summary = "Get Active Journeys", description = "Method that gets all active Journeys")
    @GetMapping(path = "/active")
    public ResponseEntity<java.util.List<JourneyDto>> getActiveJourneys() {
        java.util.List<JourneyDto> journeys = journeyService.getActiveJourneys();
        return ResponseEntity.ok(journeys);
    }

    @Operation(summary = "Create Journey", description = "Method that creates a Journey")
    @PostMapping(path = "/create")
    public ResponseEntity<Long> createJourney(@RequestBody JourneyDto journeyDto) {
        Long id = journeyService.createJourney(journeyDto);
        return ResponseEntity.status(201).body(id);
    }

    @Operation(summary = "Update Journey", description = "Method that updates a Journey")
    @PutMapping(path = "/update")
    public ResponseEntity<Void> updateJourney( @RequestBody JourneyDto journeyDto) {
        journeyService.updateJourney(journeyDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Journey", description = "Method that deletes a Journey")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteJourney(@PathVariable(name = "id") Long id) {
        journeyService.deleteJourney(id);
        return ResponseEntity.noContent().build();
    }
}
