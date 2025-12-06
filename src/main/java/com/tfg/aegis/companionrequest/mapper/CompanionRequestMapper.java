package com.tfg.aegis.companionrequest.mapper;

import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.companionrequest.model.CompanionRequestDto;

public interface CompanionRequestMapper {
    CompanionRequest toEntity(CompanionRequestDto companionRequestDto);

    CompanionRequestDto toDto(CompanionRequest companionRequest);
}
