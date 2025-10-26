package com.tfg.aegis.participation;

import com.tfg.aegis.participation.model.ParticipationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Participation", description = "API of participations")
@RestController
@AllArgsConstructor
@RequestMapping("/participation")
public class ParticipationController {
    private final ParticipationService participationService;

    @Operation(summary = "Get Participation", description = "Method that gets a Participation")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ParticipationDto> getParticipation(@PathVariable (name = "id") Long id) {
        ParticipationDto participationDto = participationService.getParticipation(id);
        return ResponseEntity.ok(participationDto);
    }

    @Operation(summary = "Create Participation", description = "Method that creates a Participation")
    @PostMapping("/create")
    public ResponseEntity<Long> createParticipation(@RequestBody ParticipationDto participationDto) {
        Long id = participationService.createParticipation(participationDto);
        return ResponseEntity.status(201).body(id);
    }

    @Operation(summary = "Update Participation", description = "Method that updates a Participation")
    @PutMapping("/update")
    public ResponseEntity<Void> updateParticipation(@RequestBody ParticipationDto participationDto) {
        participationService.updateParticipation(participationDto);
        return ResponseEntity.noContent().build();
    }
}
