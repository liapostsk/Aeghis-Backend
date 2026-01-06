package com.tfg.aegis.controller;

import com.tfg.aegis.service.SafeLocationService;
import com.tfg.aegis.model.dto.SafeLocationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@Tag(name = "Safe Locations", description = "Safe locations API")
@RestController
@RequestMapping("/safe-locations")
public class SafeLocationController {

    private final SafeLocationService safeLocationService;

    public SafeLocationController(SafeLocationService safeLocationService) {
        this.safeLocationService = safeLocationService;
    }

    @Operation(summary = "Get safe location", description = "Gets a safe location for the authenticated user")
    @GetMapping("/{id}")
    public ResponseEntity<SafeLocationDto> getSafeLocationForCurrentUser(@PathVariable Long id) {
        SafeLocationDto safeLocationDto = safeLocationService.getSafeLocationForCurrentUser(id);
        return ResponseEntity.ok(safeLocationDto);
    }

    @Operation(summary = "Create safe location", description = "Creates a new safe location for the authenticated user")
    @PostMapping
    public ResponseEntity<Long> addSafeLocationForCurrentUser(@RequestBody SafeLocationDto safeLocationDto) {
        Long id = safeLocationService.addSafeLocationForCurrentUser(safeLocationDto);
        return ResponseEntity.status(201).body(id);
    }

    @Operation(summary = "Update safe location", description = "Updates an existing safe location for the authenticated user")
    @PutMapping("/{id}")
    public ResponseEntity<Void> editSafeLocationForCurrentUser(@PathVariable Long id, @RequestBody SafeLocationDto safeLocationDto) {
        safeLocationService.editSafeLocationForCurrentUser(id, safeLocationDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete safe location", description = "Deletes a safe location for the authenticated user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSafeLocationForCurrentUser(@PathVariable Long id) {
        safeLocationService.deleteSafeLocationForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}