package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.Valoracion;
import com.tfg.aegis.model.dto.ValoracionDto;

public interface ValoracionMapper {
    Valoracion toEntity(ValoracionDto valoracionDto);

    ValoracionDto toDto(Valoracion valoracion);
}
