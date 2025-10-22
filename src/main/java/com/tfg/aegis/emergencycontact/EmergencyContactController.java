package com.tfg.aegis.emergencycontact;

import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/me/emergency-contact")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    private static final Logger log = LoggerFactory.getLogger(EmergencyContactController.class);

    @Operation(summary = "Add", description = "Add a new EmergencyContact for the current user")
    @PostMapping("add")
    public ResponseEntity<EmergencyContactDto> addEmergencyContactForCurrentUser(@RequestBody EmergencyContactDto emergencyContactDto) {
        EmergencyContactDto dto = emergencyContactService.addEmergencyContactForCurrentUser(emergencyContactDto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Edit", description = "Method that edits an existing EmergencyContact")
    @PutMapping("/{id}/edit")
    public ResponseEntity<Void> editEmergencyContact(@PathVariable Long id, @RequestBody EmergencyContactDto emergencyContactDto) {
        emergencyContactService.editEmergencyContact(id, emergencyContactDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete", description = "Method that delete an EmergencyContact")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable Long id) {
        emergencyContactService.deleteEmergencyContactForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}
