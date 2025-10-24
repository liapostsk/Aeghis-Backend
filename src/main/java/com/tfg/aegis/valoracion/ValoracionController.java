package com.tfg.aegis.valoracion;

import com.tfg.aegis.valoracion.model.ValoracionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Valoracion", description = "API of Valoraciones")
@RequestMapping(value = "/valoracion")
@RestController
@AllArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;

    private static final Logger log = LoggerFactory.getLogger(ValoracionController.class);


    @Operation(summary = "Get Valoracion", description = "Get the valoracion details")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ValoracionDto> getValoracion(@PathVariable(name = "id") Long id) {
        ValoracionDto valoracionDto = valoracionService.getValoracion(id);
        log.info("Current valoracion: {}", valoracionDto);
        return ResponseEntity.ok(valoracionDto);
    }

    @Operation(summary = "Create Valoracion", description = "Create a new valoracion")
    @GetMapping(path = "/create/{participationId}")
    public ResponseEntity<Long> createValoracion(@PathVariable(name = "participationId") Long participationId, @RequestBody ValoracionDto valoracionDto) {
        Long id = valoracionService.createValoracion(participationId, valoracionDto);
        log.info("Created valoracion id: {}", id);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Update Valoracion", description = "Update an existing valoracion")
    @GetMapping(path = "/update")
    public ResponseEntity<Void> updateValoracion(@RequestBody ValoracionDto valoracionDto) {
        valoracionService.updateValoracion(valoracionDto);
        log.info("Updated valoracion: {}", valoracionDto);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete Valoracion", description = "Delete an existing valoracion")
    @GetMapping(path = "/delete/{id}")
    public ResponseEntity<Void> deleteValoracion(@PathVariable(name = "id") Long id) {
        valoracionService.deleteValoracion(id);
        log.info("Deleted valoracion id: {}", id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
