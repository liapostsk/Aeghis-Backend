package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.service.ExternalContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ExternalContact", description = "API of external contacts")
@RestController
@AllArgsConstructor
@RequestMapping("/me/external-contact")
public class ExternalContactController {

    private final ExternalContactService externalContactService;

    @Operation(summary = "Create", description = "Create a new ExternalContact for the current user")
    @PostMapping("create")
    public ResponseEntity<Long> createExternalContactForCurrentUser(@RequestBody ExternalContactDto externalContactDto) {
        Long id = externalContactService.createExternalContactForCurrentUser(externalContactDto);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Edit", description = "Method that edits an existing EmergencyContact")
    @PutMapping("/{id}/edit")
    public ResponseEntity<Void> editExternalContact(@PathVariable Long id, @RequestBody ExternalContactDto externalContactDto) {
        externalContactService.editExternalContact(id, externalContactDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete", description = "Method that delete an EmergencyContact")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteExternalContact(@PathVariable Long id) {
        externalContactService.deleteExternalContact(id);
        return ResponseEntity.noContent().build();
    }

}
