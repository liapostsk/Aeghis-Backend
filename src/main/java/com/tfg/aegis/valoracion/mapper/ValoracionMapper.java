package com.tfg.aegis.valoracion.mapper;

import com.tfg.aegis.valoracion.model.Valoracion;
import com.tfg.aegis.valoracion.model.ValoracionDto;

public interface ValoracionMapper {
    Valoracion toEntity(ValoracionDto valoracionDto);

    ValoracionDto toDto(Valoracion valoracion);
}
