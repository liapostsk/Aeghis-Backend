package com.tfg.aegis.valoracion.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ValoracionDto {
    private Long id;
    private Enums.TipoValoracion valor;
    private String comentario;
    private LocalDateTime fecha;
}