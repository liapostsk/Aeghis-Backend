package com.tfg.aegis.person.externalcontact.model;

import com.tfg.aegis.person.model.PersonDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExternalContactDto extends PersonDto {
    private String relation;
}
