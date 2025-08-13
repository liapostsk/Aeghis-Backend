package com.tfg.aegis.safelocation;

import com.tfg.aegis.safelocation.model.SafeLocationDto;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/safe-location")
public class SafeLocationController {

    private final SafeLocationService safeLocationService;

    private static final Logger log = LoggerFactory.getLogger(SafeLocationController.class);

    public SafeLocationController(SafeLocationService safeLocationService) {
        this.safeLocationService = safeLocationService;
    }

    @Operation(summary = "Add", description = "Add a new SafeLocation for the current user")
    @PostMapping("/add")
    public ResponseEntity<Long> addSafeLocationForCurrentUser(@RequestBody SafeLocationDto safeLocationDto) {
        safeLocationService.addSafeLocationForCurrentUser(safeLocationDto);
        return ResponseEntity.ok(safeLocationDto.getId());
    }

    @Operation(summary = "Edit", description = "Method that edits an existing SafeLocation")
    @PutMapping("/{id}/edit")
    public ResponseEntity<Void> editSafeLocationForCurrentUser(@PathVariable Long id, @RequestBody SafeLocationDto safeLocationDto) {
        safeLocationService.editSafeLocationForCurrentUser(id, safeLocationDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete", description = "Method that delete a SafeLocation")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteSafeLocationForCurrentUser(@PathVariable Long id) {
        safeLocationService.deleteSafeLocationForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}