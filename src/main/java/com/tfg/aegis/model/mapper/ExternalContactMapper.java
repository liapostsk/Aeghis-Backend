package com.tfg.aegis.model.mapper;

import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.model.dto.ExternalContactDto;

public interface ExternalContactMapper {
    ExternalContactDto toDto(ExternalContact externalContact);

    ExternalContact toEntity(ExternalContactDto externalContactDto);
}
