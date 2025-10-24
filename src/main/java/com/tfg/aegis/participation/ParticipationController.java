package com.tfg.aegis.participation;

import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.participation.model.ParticipationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Participation", description = "API of participations")
@RestController
@AllArgsConstructor
@RequestMapping("/participation")
public class ParticipationController {
    private final ParticipationService participationService;

    @Operation(summary = "Get Participation", description = "Method that gets a Participation")
    @GetMapping(path = "{id}")
    public ResponseEntity<ParticipationDto> getParticipation(Long id) {
        ParticipationDto participationDto = participationService.getParticipation(id);
        return ResponseEntity.ok(participationDto);
    }

    @Operation(summary = "Create Participation", description = "Method that creates a Participation")
    public ResponseEntity<Long> createParticipation(ParticipationDto participationDto) {
        Long id = participationService.createParticipation(participationDto);
        return ResponseEntity.status(201).body(id);
    }

    @Operation(summary = "Update Participation", description = "Method that updates a Participation")
    public ResponseEntity<Void> updateParticipation(ParticipationDto participationDto) {
        participationService.updateParticipation(participationDto);
        return ResponseEntity.noContent().build();
    }
}
