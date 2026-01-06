package com.tfg.aegis.controller;

import com.tfg.aegis.service.ParticipationService;
import com.tfg.aegis.model.dto.ParticipationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Participation", description = "API of participations")
@RestController
@AllArgsConstructor
@RequestMapping("/participations")
public class ParticipationController {
    private final ParticipationService participationService;

    @Operation(summary = "Get Participation", description = "Method that gets a Participation")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ParticipationDto> getParticipation(@PathVariable (name = "id") Long id) {
        ParticipationDto participationDto = participationService.getParticipation(id);
        return ResponseEntity.ok(participationDto);
    }

    @Operation(summary = "Create Participation", description = "Method that creates a Participation")
    @PostMapping
    public ResponseEntity<Long> createParticipation(@RequestBody ParticipationDto participationDto) {
        Long id = participationService.createParticipation(participationDto);
        return ResponseEntity.status(201).body(id);
    }

    @Operation(summary = "Update Participation", description = "Method that updates a Participation")
    @PutMapping
    public ResponseEntity<Void> updateParticipation(@RequestBody ParticipationDto participationDto) {
        if (participationDto.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        participationService.updateParticipation(participationDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Participation", description = "Method that deletes a Participation")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipation(@PathVariable Long id) {
        participationService.deleteParticipation(id);
        return ResponseEntity.noContent().build();
    }
}
