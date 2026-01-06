package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.EmergencyTriggerRequestDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.service.EmergencyAlertService;
import com.tfg.aegis.service.EmergencyContactService;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Emergency Contacts", description = "Emergency contacts management API")
@RestController
@AllArgsConstructor
@RequestMapping("/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;
    private final UserService userService;
    private final EmergencyAlertService emergencyAlertService;

    private static final Logger log = LoggerFactory.getLogger(EmergencyContactController.class);

    @Operation(summary = "Get emergency contact", description = "Gets an emergency contact for the authenticated user")
    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactDto> getEmergencyContactForCurrentUser(@PathVariable Long id) {
        EmergencyContactDto emergencyContactDto = emergencyContactService.getEmergencyContactForCurrentUser(id);
        return ResponseEntity.ok(emergencyContactDto);
    }

    @Operation(summary = "Create emergency contact", description = "Creates a new emergency contact for the authenticated user")
    @PostMapping
    public ResponseEntity<EmergencyContactDto> addEmergencyContactForCurrentUser(@RequestBody EmergencyContactDto emergencyContactDto) {
        EmergencyContactDto dto = emergencyContactService.addEmergencyContactForCurrentUser(emergencyContactDto);
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(summary = "Update emergency contact", description = "Updates an existing emergency contact")
    @PutMapping("/{id}")
    public ResponseEntity<Void> editEmergencyContact(@PathVariable Long id, @RequestBody EmergencyContactDto emergencyContactDto) {
        emergencyContactService.editEmergencyContact(id, emergencyContactDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete emergency contact", description = "Deletes an emergency contact")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable Long id) {
        emergencyContactService.deleteEmergencyContactForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger emergency alert", description = "Sends an emergency push notification to all accepted emergency contacts")
    @PostMapping("/alert")
    public ResponseEntity<Void> triggerEmergency(@RequestBody EmergencyTriggerRequestDto req) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto me = userService.getUserByClerkId(clerkId);

        emergencyAlertService.trigger(me.getId(), me.getName(), req);
        log.info("Emergency triggered by user ID {}: {}", me.getId(), req);
        return ResponseEntity.noContent().build();
    }
}
