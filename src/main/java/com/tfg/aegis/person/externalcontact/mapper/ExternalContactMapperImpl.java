package com.tfg.aegis.person.externalcontact.mapper;

import com.tfg.aegis.person.externalcontact.model.ExternalContact;
import com.tfg.aegis.person.externalcontact.model.ExternalContactDto;
import org.springframework.stereotype.Component;

@Component
public class ExternalContactMapperImpl implements ExternalContactMapper {
    public ExternalContactDto toDto(ExternalContact externalContact) {
        if (externalContact == null) {
            return null;
        }

        ExternalContactDto externalContactDto = new ExternalContactDto();

        externalContactDto.setId(externalContact.getId());
        externalContactDto.setName(externalContact.getName());
        externalContactDto.setPhone(externalContact.getPhone());
        externalContactDto.setRelation(externalContact.getRelation());

        return externalContactDto;
    }

    public ExternalContact toEntity(ExternalContactDto externalContactDto) {
        if (externalContactDto == null) {
            return null;
        }

        ExternalContact externalContact = new ExternalContact();

        externalContact.setName(externalContactDto.getName());
        externalContact.setPhone(externalContactDto.getPhone());
        externalContact.setRelation(externalContactDto.getRelation());

        return externalContact;
    }
}
