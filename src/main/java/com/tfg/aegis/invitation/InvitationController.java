package com.tfg.aegis.invitation;

import com.tfg.aegis.invitation.model.InvitationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Group", description = "API of groups")
@RestController
@RequestMapping("/invitation")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService groupService) {
        this.invitationService = groupService;
    }

    @PostMapping("/{groupId}/invite")
    @Operation(summary= "create an invitation", description = "Creates an invitation for a group")
    public ResponseEntity<InvitationDto> createInvitation(@PathVariable Long groupId) {
        InvitationDto invitationDto = invitationService.createInvitation(groupId, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationDto);
    }
    
    @PostMapping("/{groupId}/validate")
    @Operation(summary = "validate an invitation code", description = "Validates an invitation code")
    public ResponseEntity<Boolean> validateInvitation(@PathVariable Long groupId, @RequestParam String code) {
        Boolean allowed = invitationService.validateInvitation(groupId, code);
        return ResponseEntity.ok(allowed);
    }
}
