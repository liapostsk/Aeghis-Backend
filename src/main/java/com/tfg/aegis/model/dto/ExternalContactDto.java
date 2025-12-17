package com.tfg.aegis.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExternalContactDto extends PersonDto {
    private String relation;
}
