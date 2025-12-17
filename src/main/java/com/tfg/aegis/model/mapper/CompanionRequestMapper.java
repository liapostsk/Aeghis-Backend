package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.dto.CompanionRequestDto;

public interface CompanionRequestMapper {
    CompanionRequest toEntity(CompanionRequestDto companionRequestDto);

    CompanionRequestDto toDto(CompanionRequest companionRequest);
}
