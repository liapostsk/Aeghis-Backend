package com.tfg.aegis.valoracion.mapper;

import com.tfg.aegis.valoracion.model.Valoracion;
import com.tfg.aegis.valoracion.model.ValoracionDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ValoracionMapperImpl implements ValoracionMapper {

    @Override
    public Valoracion toEntity(ValoracionDto dto) {
        if (dto == null) {
            return null;
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setComentario(dto.getComentario());
        valoracion.setFecha(dto.getFecha());
        valoracion.setValor(dto.getValor());
        return valoracion;
    }

    @Override
    public ValoracionDto toDto(Valoracion valoracion) {
        if (valoracion == null) {
            return null;
        }

        ValoracionDto dto = new ValoracionDto();
        dto.setComentario(valoracion.getComentario());
        dto.setFecha(valoracion.getFecha());
        dto.setValor(valoracion.getValor());
        return dto;
    }
}
