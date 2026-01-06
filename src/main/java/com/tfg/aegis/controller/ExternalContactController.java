package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.service.ExternalContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "External Contacts", description = "External contacts API")
@RestController
@AllArgsConstructor
@RequestMapping("/external-contacts")
public class ExternalContactController {

    private final ExternalContactService externalContactService;

    @Operation(summary = "Get external contact", description = "Retrieves an external contact by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExternalContactDto> getExternalContactForCurrentUser(@PathVariable Long id) {
        ExternalContactDto externalContactDto = externalContactService.getExternalContactForCurrentUser(id);
        return ResponseEntity.ok(externalContactDto);
    }

    @Operation(summary = "Create external contact", description = "Creates a new external contact")
    @PostMapping
    public ResponseEntity<Long> createExternalContactForCurrentUser(@RequestBody ExternalContactDto externalContactDto) {
        Long id = externalContactService.createExternalContactForCurrentUser(externalContactDto);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Update external contact", description = "Updates an existing external contact")
    @PutMapping("/{id}")
    public ResponseEntity<Void> editExternalContact(@PathVariable Long id, @RequestBody ExternalContactDto externalContactDto) {
        externalContactService.editExternalContact(id, externalContactDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete external contact", description = "Deletes an external contact")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExternalContact(@PathVariable Long id) {
        externalContactService.deleteExternalContact(id);
        return ResponseEntity.noContent().build();
    }

}
