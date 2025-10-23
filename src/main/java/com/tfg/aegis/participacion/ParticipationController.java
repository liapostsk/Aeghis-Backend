package com.tfg.aegis.participacion;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Participation", description = "API of participations")
@RestController
@AllArgsConstructor
@RequestMapping("/participation")
public class ParticipationController {
    private final ParticipationService participationService;


}
