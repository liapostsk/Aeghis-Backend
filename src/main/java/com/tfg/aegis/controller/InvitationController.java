package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.service.InvitationService;
import com.tfg.aegis.model.dto.InvitationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invitation", description = "API of invitations")
@RestController
@RequestMapping("/invitations")
@AllArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping("/{groupId}/invite")
    @Operation(summary= "create an invitation", description = "Creates an invitation for a group")
    public ResponseEntity<InvitationDto> createInvitation(@PathVariable Long groupId, @RequestParam(required = false) Long expiry) {
        InvitationDto invitationDto = invitationService.createInvitation(groupId, expiry);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationDto);
    }
    
    @PostMapping("/validate")
    @Operation(summary = "validate an invitation code", description = "Validates an invitation code")
    public ResponseEntity<GroupDto> validateInvitation(@RequestParam String code) {
        GroupDto allowed = invitationService.validateInvitation(code);
        return ResponseEntity.ok(allowed);
    }
}
