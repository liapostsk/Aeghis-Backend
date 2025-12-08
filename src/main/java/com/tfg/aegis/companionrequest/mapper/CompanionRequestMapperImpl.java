package com.tfg.aegis.companionrequest.mapper;

import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.companionrequest.model.CompanionRequestDto;
import com.tfg.aegis.companionrequest.model.Enums;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CompanionRequestMapperImpl implements CompanionRequestMapper {

    @Override
    public CompanionRequestDto toDto(CompanionRequest companionRequest) {
        if (companionRequest == null) {
            return null;
        }

        CompanionRequestDto dto = new CompanionRequestDto();
        dto.setId(companionRequest.getId());
        dto.setState(companionRequest.getState() != null ? companionRequest.getState() : Enums.RequestStatus.CREATED);
        dto.setAproxHour(companionRequest.getAproxHour());
        dto.setDescription(companionRequest.getDescription());
        dto.setCreationDate(companionRequest.getCreationDate());
        dto.setCompanionId(companionRequest.getCompanion() != null ? companionRequest.getCompanion().getId() : null);
        // source, destination, creator and companionId are intentionally not set here

        return dto;
    }

    @Override
    public CompanionRequest toEntity(CompanionRequestDto companionRequestDto) {
        if (companionRequestDto == null) {
            return null;
        }
        CompanionRequest companionRequest = new CompanionRequest();
        companionRequest.setState(companionRequestDto.getState());
        companionRequest.setAproxHour(companionRequestDto.getAproxHour());
        companionRequest.setDescription(companionRequestDto.getDescription());
        companionRequest.setCreationDate(companionRequestDto.getCreationDate());
        // source, destination, creator and companionId are intentionally not set here
        return companionRequest;
    }
}
