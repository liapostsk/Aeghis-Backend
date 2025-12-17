package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.EmergencyTriggerRequestDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.service.EmergencyAlertService;
import com.tfg.aegis.service.EmergencyContactService;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/me/emergency-contact")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;
    private final UserService userService;
    private final EmergencyAlertService emergencyAlertService;

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

    @Operation(summary = "Trigger emergency", description = "Send an emergency push notification to all ACCEPTED emergency contacts")
    @PostMapping("/trigger")
    public ResponseEntity<Void> triggerEmergency(@RequestBody EmergencyTriggerRequestDto req) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto me = userService.getUserByClerkId(clerkId);

        emergencyAlertService.trigger(me.getId(), me.getName(), req);
        log.info("Emergency triggered by user ID {}: {}", me.getId(), req);
        return ResponseEntity.noContent().build();
    }
}
