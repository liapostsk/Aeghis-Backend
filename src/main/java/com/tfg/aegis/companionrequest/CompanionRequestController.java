package com.tfg.aegis.companionrequest;

import com.tfg.aegis.companionrequest.model.CompanionRequestDto;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
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
import java.util.Collections;
import java.util.List;

@Tag(name = "CompanionRequest", description = "API of companion requests")
@RequestMapping(value = "/companion-request")
@RestController
@AllArgsConstructor
public class CompanionRequestController {

    private final CompanionRequestService companionRequestService;

    private static final Logger log = LoggerFactory.getLogger(CompanionRequestController.class);

    @Operation(summary = "Create Companion Request", description = "Method that creates a Companion Request")
    @PostMapping
    public ResponseEntity<Long> createCompanionRequest(@RequestBody CompanionRequestDto dto) {
        Long requestDto = companionRequestService.createCompanionRequest(dto);
        log.info("New companion request created");
        return ResponseEntity.ok(requestDto);
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

    @Operation(summary = "Get my Companion Requests", description = "Method that gets my Companion Requests")
    @GetMapping("/my-requests")
    public ResponseEntity<List<CompanionRequestDto>> getMyCompanionRequests() {
        List<CompanionRequestDto> requests = companionRequestService.getMyCompanionRequests();
        log.info("Retrieved my companion requests");
        return ResponseEntity.ok(requests);
    }

    @Operation(summary = "Accept Companion Request", description = "Method that accepts a Companion Request")
    @PostMapping("/{id}/accept")
    public ResponseEntity<CompanionRequestDto> accept(@PathVariable Long id, @RequestBody(required = false) GroupDto groupDto) {
        CompanionRequestDto dto = companionRequestService.acceptCompanionRequest(id, groupDto);
        log.info("Accepted a companion request");
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Decline Companion Request", description = "Method that declines a Companion Request")
    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> declineCompanionRequest(@PathVariable Long id) {
        companionRequestService.declineCompanionRequest(id);
        log.info("Declined a companion request");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel Companion Request", description = "Method that cancels a Companion Request")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelCompanionRequest(@PathVariable Long id) {
        companionRequestService.cancelCompanionRequest(id);
        log.info("Cancelled a companion request");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Initiate journey for Companion Request", description = "Method that initiates a journey for a Companion Request")
    @PostMapping("{id}/initiate-journey")
    public ResponseEntity<CompanionRequestDto> initiateJourneyForCompanionRequest(@PathVariable Long id, @RequestBody(required = false) JourneyDto journeyDto) {
        CompanionRequestDto dto = companionRequestService.startJourney(id, journeyDto);
        log.info("Initiated journey for companion request");
        return ResponseEntity.ok(dto);
    }

}
