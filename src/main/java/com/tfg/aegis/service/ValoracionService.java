package com.tfg.aegis.service;

import com.tfg.aegis.repository.ValoracionRepository;
import com.tfg.aegis.model.mapper.ValoracionMapper;
import com.tfg.aegis.model.entity.Valoracion;
import com.tfg.aegis.model.dto.ValoracionDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class ValoracionService {
    private final ValoracionRepository valoracionRepository;

    private final ValoracionMapper valoracionMapper;

    /**
     * Method that gets a Valoracion
     * @param id
     * @return
     */
    public ValoracionDto getValoracion(Long id) {
        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoracion with id %s not found".formatted(id)));
        return valoracionMapper.toDto(valoracion);
    }

    public Long createValoracion(Long participationId, ValoracionDto valoracionDto) {
        Valoracion valoracion = valoracionMapper.toEntity(valoracionDto);
        valoracion.setParticipationId(participationId);
        Valoracion savedValoracion = valoracionRepository.save(valoracion);
        return savedValoracion.getId();
    }

    public void updateValoracion(ValoracionDto valoracionDto) {
        Valoracion existingValoracion = valoracionRepository.findById(valoracionDto.getId())
                .orElseThrow(() -> new RuntimeException("Valoracion with id %s not found".formatted(valoracionDto.getId())));

        existingValoracion.setValor(valoracionDto.getValor());
        existingValoracion.setComentario(valoracionDto.getComentario());
        existingValoracion.setFecha(valoracionDto.getFecha());

        valoracionRepository.save(existingValoracion);
    }

    public void deleteValoracion(Long id) {
        Valoracion existingValoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoracion with id %s not found".formatted(id)));
        valoracionRepository.delete(existingValoracion);
    }
}
