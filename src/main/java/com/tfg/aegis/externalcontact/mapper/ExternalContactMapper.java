package com.tfg.aegis.externalcontact.mapper;

import com.tfg.aegis.externalcontact.model.ExternalContact;
import com.tfg.aegis.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.user.model.User;

public interface ExternalContactMapper {
    ExternalContactDto toDto(ExternalContact externalContact);

    ExternalContact toEntity(ExternalContactDto externalContactDto);
}
