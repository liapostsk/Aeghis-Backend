package com.tfg.aegis.companionrequest;

import com.tfg.aegis.companionrequest.model.CompanionRequestDto;
import com.tfg.aegis.companionrequest.model.CreateCompanionRequestDto;
import com.tfg.aegis.journey.model.JourneyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "CompanionRequest", description = "API of companion requests")
@RequestMapping(value = "/companion-request")
@RestController
@AllArgsConstructor
public class CompanionRequestController {

    private final CompanionRequestService companionRequestService;

    private static final Logger log = LoggerFactory.getLogger(CompanionRequestController.class);

    /**
     * Endpoint for the creator
     */

    @Operation(summary = "Create Companion Request", description = "Method that creates a Companion Request")
    @PostMapping
    public ResponseEntity<Long> createCompanionRequest(@RequestBody CreateCompanionRequestDto dto) {
        Long requestDto = companionRequestService.createCompanionRequest(dto);
        log.info("New companion request created");
        return ResponseEntity.ok(requestDto);
    }

    @Operation(summary = "Accept Companion Request", description = "Method that accepts a Companion Request")
    @PostMapping("/{id}/accept")
    public ResponseEntity<CompanionRequestDto> accept(@PathVariable Long id) {
        // TODO: Si el grupo tracking se escoge cuando se comparte el trayecto no hace falta pasarlo aqui, eliminar el parametro groupDto
        CompanionRequestDto dto = companionRequestService.acceptCompanionRequest(id);
        log.info("Accepted a companion request");
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Reject Companion Request", description = "Method that rejects a Companion Request")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectCompanionRequest(@PathVariable Long id) {
        companionRequestService.rejectCompanionRequest(id);
        log.info("Declined a companion request");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Companion Request", description = "Method that deletes a Companion Request")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanionRequest(@PathVariable Long id) {
        companionRequestService.deleteCompanionRequest(id);
        log.info("Deleted a companion request");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary= "Finish Companion Request", description= "Method that finishes a Companion Request")
    @PostMapping("/{id}/finish")
    public ResponseEntity<CompanionRequestDto> finishCompanionRequest(@PathVariable Long id) {
        CompanionRequestDto dto = companionRequestService.finishCompanionRequest(id);
        log.info("Finished a companion request");
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Creates an individual journey for the companion", description = "Method that creates an individual journey for the companion")
    @PostMapping("/{id}/submit-journey")
    public ResponseEntity<CompanionRequestDto> submitIndividualJourney(@PathVariable Long id, @RequestBody JourneyDto journeyDto) {
        CompanionRequestDto dto = companionRequestService.submitIndividualJourney(id, journeyDto);
        log.info("Created individual journey for companion");
        return ResponseEntity.ok(dto);
    }

    /**
     * Endpoint for the searchers
     */
    @Operation(summary = "Get my Companion Requests", description = "Method that gets my Companion Requests")
    @GetMapping("/my-requests")
    public ResponseEntity<List<CompanionRequestDto>> getMyCompanionRequests() {
        List<CompanionRequestDto> requests = companionRequestService.getMyCompanionRequests();
        log.info("Retrieved my companion requests");
        return ResponseEntity.ok(requests);
    }

    @Operation(summary= "List active Companion Requests", description= "Method that lists all active Companion Requests")
    @GetMapping("/available")
    public ResponseEntity<List<CompanionRequestDto>> listActiveCompanionRequests() {
        List<CompanionRequestDto> requests = companionRequestService.listActiveCompanionRequests();
        log.info("Listed active companion requests");
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Search Companion Requests", description = "Method that searches for Companion Requests")
    @GetMapping("/search")
    public ResponseEntity<List<CompanionRequestDto>> searchCompanionRequests(
            @RequestParam(required = false) Long destinoId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "true") boolean excludeMine
    ) {
        List<CompanionRequestDto> requests = companionRequestService.searchCompanionRequests(destinoId, from, to, excludeMine);
        log.info("Searched for companion requests with criteria - destino: {}, from: {}, to: {}, excludeMine: {}", destinoId, from, to, excludeMine);
        log.info("Found companion requests {}", requests);
        return ResponseEntity.ok(requests);
    }

    //searcher ask for joining to the companion request
    @Operation(summary = "Request to join Companion Request", description = "Method that requests to join a Companion Request")
    @PostMapping("/{id}/request-join")
    public ResponseEntity<Void> requestToJoinCompanionRequest(@PathVariable Long id) {
        companionRequestService.requestToJoinCompanionRequest(id);
        log.info("Requested to join a companion request");
        return ResponseEntity.noContent().build();
    }

    //searcher cancels the request to join the companion request
    @Operation(summary = "Cancel Companion Request", description = "Method that cancels a Companion Request")
    @PostMapping("/{id}/request-join/cancel")
    public ResponseEntity<Void> cancelCompanionRequest(@PathVariable Long id) {
        companionRequestService.cancelCompanionRequest(id);
        log.info("Cancelled a companion request");
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for both the creator and the searcher
     */

    @Operation(summary = "Get Companion Request by ID", description = "Method that gets a Companion Request by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompanionRequestDto> getCompanionRequestById(@PathVariable Long id) {
        CompanionRequestDto dto = companionRequestService.getCompanionRequestById(id);
        log.info("Retrieved companion request by ID");
        return ResponseEntity.ok(dto);
    }

}
