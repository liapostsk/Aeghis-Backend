package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateCompanionRequestMapperImpl implements CreateCompanionRequestMapper {

    @Override
    public CreateCompanionRequestDto toDto(CompanionRequest companionRequest) {
        if (companionRequest == null) {
            return null;
        }

        CreateCompanionRequestDto dto = new CreateCompanionRequestDto();
        dto.setAproxHour(companionRequest.getAproxHour());
        dto.setDescription(companionRequest.getDescription());
        dto.setSourceId(companionRequest.getSource().getId());
        dto.setDestinationId(companionRequest.getDestination().getId());

        return dto;
    }

    @Override
    public CompanionRequest toEntity(CreateCompanionRequestDto createCompanionRequestDto) {
        if (createCompanionRequestDto == null) {
            return null;
        }
        CompanionRequest companionRequest = new CompanionRequest();
        companionRequest.setAproxHour(createCompanionRequestDto.getAproxHour());
        companionRequest.setDescription(createCompanionRequestDto.getDescription());

        return companionRequest;
    }
}
