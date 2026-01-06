package com.tfg.aegis.controller;

import com.tfg.aegis.model.enums.CompanionRequestEnums;
import com.tfg.aegis.service.CompanionRequestService;
import com.tfg.aegis.model.dto.CompanionRequestDto;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;
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
@RequestMapping(value = "/companion-requests")
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

    @Operation(summary = "Delete Companion Request", description = "Method that deletes a Companion Request")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanionRequest(@PathVariable Long id) {
        companionRequestService.deleteCompanionRequest(id);
        log.info("Deleted a companion request");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "Change Companion Request status", description = "Changes the status of a Companion Request (MATCHED, CREATED, FINISHED)")
    public ResponseEntity<CompanionRequestDto> changeStatus(@PathVariable Long id, @PathVariable CompanionRequestEnums.RequestStatus status) {
        CompanionRequestDto dto = companionRequestService.changeStatus(id, status);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Edit the information of a Companion Request", description = "Method that edits the information of a Companion Request")
    @PutMapping("/{id}")
    public ResponseEntity<CompanionRequestDto> editCompanionRequest(@PathVariable Long id, @RequestBody CreateCompanionRequestDto dto) {
        CompanionRequestDto updatedDto = companionRequestService.editCompanionRequest(id, dto);
        log.info("Edited companion request with id {}", id);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Link a Group to a Companion Request", description = "Method that links a Group to a Companion Request")
    @PostMapping("/{id}/link-group/{groupId}")
    public ResponseEntity<CompanionRequestDto> linkGroupToCompanionRequest(@PathVariable Long id, @PathVariable Long groupId) {
        CompanionRequestDto updatedDto = companionRequestService.linkGroupToCompanionRequest(id, groupId);
        log.info("Linked group {} to companion request {}", groupId, id);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Get the companion request by companion group ID", description = "Method that gets the companion request by companion group ID")
    @GetMapping("/by-companion-group/{groupId}")
    public ResponseEntity<CompanionRequestDto> getCompanionRequestByCompanionGroupId(@PathVariable Long groupId) {
        CompanionRequestDto dto = companionRequestService.getCompanionRequestByCompanionGroupId(groupId);
        log.info("Retrieved companion request by companion group ID {}", groupId);
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

    @Operation(summary = "Request to join Companion Request", description = "Creates a join request")
    @PostMapping("/{id}/join-request")
    public ResponseEntity<Void> requestToJoinCompanionRequest(@PathVariable Long id, @RequestBody(required = false) String message) {
        companionRequestService.requestToJoinCompanionRequest(id, message);
        log.info("Requested to join companion request {} with message {}", id, message);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel join request", description = "Cancels the join request")
    @DeleteMapping("/{id}/join-request")
    public ResponseEntity<Void> cancelCompanionRequest(@PathVariable Long id) {
        companionRequestService.cancelCompanionRequest(id);
        log.info("Cancelled join request for companion request {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for both the creator and the searcher
     */

    @Operation(summary = "Get companion request by ID", description = "Retrieves detailed information about a companion request by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompanionRequestDto> getCompanionRequestById(@PathVariable Long id) {
        CompanionRequestDto dto = companionRequestService.getCompanionRequestById(id);
        log.info("Retrieved companion request by ID");
        return ResponseEntity.ok(dto);
    }

}
