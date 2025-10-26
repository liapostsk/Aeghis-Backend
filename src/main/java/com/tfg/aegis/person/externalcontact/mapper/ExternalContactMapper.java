package com.tfg.aegis.person.externalcontact.mapper;

import com.tfg.aegis.person.externalcontact.model.ExternalContact;
import com.tfg.aegis.person.externalcontact.model.ExternalContactDto;

public interface ExternalContactMapper {
    ExternalContactDto toDto(ExternalContact externalContact);

    ExternalContact toEntity(ExternalContactDto externalContactDto);
}
