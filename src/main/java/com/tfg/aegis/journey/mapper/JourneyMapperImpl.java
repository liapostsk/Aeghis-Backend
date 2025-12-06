package com.tfg.aegis.journey.mapper;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.participation.model.Participation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JourneyMapperImpl implements JourneyMapper {

    @Override
    public Journey toEntity(JourneyDto journeyDto) {
        if ( journeyDto == null ) {
            return null;
        }

        Journey journey = new Journey();

        journey.setId(journeyDto.getId());
        journey.setIniDate(journeyDto.getIniDate());
        journey.setEndDate(journeyDto.getEndDate());
        journey.setState(journeyDto.getState());
        journey.setJourneyType(journeyDto.getJourneyType());

        return journey;
    }

    @Override
    public JourneyDto toDto(Journey journey) {
        if (journey == null) {
            return null;
        }
        JourneyDto journeyDto = new JourneyDto();
        journeyDto.setId(journey.getId());
        journeyDto.setIniDate(journey.getIniDate());
        journeyDto.setEndDate(journey.getEndDate());
        journeyDto.setState(journey.getState());
        journeyDto.setJourneyType(journey.getJourneyType());
        journeyDto.setGroupId(journey.getGroup().getId());
        journeyDto.setParticipantsIds(journey.getParticipations() != null
                ? journey.getParticipations().stream().map(Participation::getId).collect(Collectors.toSet())
                : new HashSet<>());
        journeyDto.setCompanionRequestId(
                journey.getCompanionRequest() != null ? journey.getCompanionRequest().getId() : null
        );
        return journeyDto;
    }
}
