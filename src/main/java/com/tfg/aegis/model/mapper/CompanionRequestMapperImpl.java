package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.dto.CompanionRequestDto;
import com.tfg.aegis.model.enums.CompanionRequestEnums;
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
        dto.setState(companionRequest.getState() != null ? companionRequest.getState() : CompanionRequestEnums.RequestStatus.CREATED);
        dto.setAproxHour(companionRequest.getAproxHour());
        dto.setDescription(companionRequest.getDescription());
        dto.setCreationDate(companionRequest.getCreationDate());
        dto.setCompanionMessage(companionRequest.getCompanionMessage());

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
        companionRequest.setCompanionMessage(companionRequestDto.getCompanionMessage());

        return companionRequest;
    }
}
