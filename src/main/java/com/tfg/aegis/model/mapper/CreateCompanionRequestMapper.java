package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;

public interface CreateCompanionRequestMapper {
    CompanionRequest toEntity(CreateCompanionRequestDto createCompanionRequestDto);

    CreateCompanionRequestDto toDto(CompanionRequest companionRequest);
}
