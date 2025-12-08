package com.tfg.aegis.companionrequest.mapper;

import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.companionrequest.model.CreateCompanionRequestDto;

public interface CreateCompanionRequestMapper {
    CompanionRequest toEntity(CreateCompanionRequestDto createCompanionRequestDto);

    CreateCompanionRequestDto toDto(CompanionRequest companionRequest);
}
