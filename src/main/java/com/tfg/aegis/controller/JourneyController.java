package com.tfg.aegis.controller;

import com.tfg.aegis.service.JourneyService;
import com.tfg.aegis.model.dto.JourneyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Journeys", description = "Journey management API")
@RequestMapping(value = "/journeys")
@RestController
@AllArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    private static final Logger log = LoggerFactory.getLogger(JourneyController.class);

    @Operation(summary = "Get journey", description = "Retrieves a journey by ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<JourneyDto> getJourney(@PathVariable(name = "id") Long id) {
        JourneyDto journeyDto = journeyService.getJourney(id);

        log.info("Journey fetched: {}", journeyDto);
        return ResponseEntity.ok(journeyDto);
    }

    @Operation(summary = "Get current journey", description = "Retrieves the current active journey for a specific group")
    @GetMapping(path = "/current/{groupId}")
    public ResponseEntity<JourneyDto> getCurrentJourneyForGroup(@PathVariable(name = "groupId") Long groupId) {
        JourneyDto journeyDto = journeyService.getCurrentJourneyForGroup(groupId);

        log.info("Current journey for group {}: {}", groupId, journeyDto);
        return ResponseEntity.ok(journeyDto);
    }

    @Operation(summary = "Get active journeys", description = "Retrieves all active journeys for the authenticated user")
    @GetMapping(path = "/active")
    public ResponseEntity<Set<JourneyDto>> getActiveJourneys() {
        Set<JourneyDto> journeys = journeyService.getActiveJourneys();

        log.info("Get active journeys: {}", journeys);
        return ResponseEntity.ok(journeys);
    }

    @Operation(summary = "Create journey", description = "Creates a new journey")
    @PostMapping
    public ResponseEntity<Long> createJourney(@RequestBody JourneyDto journeyDto) {
        Long id = journeyService.createJourney(journeyDto);

        log.info("New journey created with id: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "Update journey", description = "Updates an existing journey")
    @PutMapping
    public ResponseEntity<Void> updateJourney( @RequestBody JourneyDto journeyDto) {
        journeyService.updateJourney(journeyDto);

        log.info("Journey updated: {}", journeyDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add participation to journey", description = "Adds a participation to an existing journey")
    @PostMapping(path = "/{journeyId}/participations/{participationId}")
    public ResponseEntity<Void> addParticipationToJourney(@PathVariable(name = "journeyId") Long journeyId,
                                                          @PathVariable(name = "participationId") Long participationId) {
        journeyService.addParticipationToJourney(journeyId, participationId);
        log.info("Participation with id: {} added to Journey with id: {}", participationId, journeyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete journey", description = "Deletes a journey by ID")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteJourney(@PathVariable(name = "id") Long id) {
        journeyService.deleteJourney(id);

        log.info("Journey deleted with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if user is participant", description = "Checks if the authenticated user is a participant in the specified journey")
    @GetMapping(path = "/{journeyId}/is-participant")
    public ResponseEntity<Boolean> isUserParticipant(@PathVariable(name = "journeyId") Long journeyId) {
        boolean isParticipant = journeyService.isUserParticipantInJourney(journeyId);
        log.info("Current user of the journey with journeyId {} is participant: {}", journeyId, isParticipant);
        return ResponseEntity.ok(isParticipant);
    }

    @Operation(summary = "Get journey participants", description = "Retrieves all participant IDs of a journey")
    @GetMapping(path = "/{journeyId}/participants")
    public ResponseEntity<Set<Long>> getAllParticipantsOfJourney(@PathVariable(name = "journeyId") Long journeyId) {
        Set<Long> participantIds = journeyService.getAllParticipantsOfJourney(journeyId);
        log.info("Participants of journey with id {}: {}", journeyId, participantIds);
        return ResponseEntity.ok(participantIds);
    }

    @Operation(summary = "Change journey status", description = "Updates the status of a journey")
    @PutMapping(path = "/{journeyId}/status/{status}")
    public ResponseEntity<Void> changeJourneyStatus(@PathVariable(name = "journeyId") Long journeyId,
                                                    @PathVariable(name = "status") String status) {
        journeyService.changeJourneyStatus(journeyId, status);
        log.info("Journey with id: {} changed status to: {}", journeyId, status);
        return ResponseEntity.noContent().build();
    }
}
