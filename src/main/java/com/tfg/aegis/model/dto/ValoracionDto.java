package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.Enums;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ValoracionDto {
    private Long id;
    private Enums.TipoValoracion valor;
    private String comentario;
    private LocalDateTime fecha;
    private Long participationId;
}