package com.tfg.aegis.group.model;

import lombok.Data;

import com.tfg.aegis.group.model.Enums.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class GroupDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl; // URL de la imagen del grupo
    private TypeGroup type; // Tipo de grupo (e.g., "confianza", "temporal", "acompañamiento")
    private GroupState state; // Estado del grupo (e.g., "activo", "inactivo")
    private LocalDateTime createdAt; // Fecha de creación del grupo
    private LocalDateTime expirationDate; // Fecha de expiración del grupo, si aplica
    private LocalDateTime lastModified; // Fecha de la última modificación del grupo
    private Long ownerId; // ID del propietario del grupo
    private Set<Long> membersIds; // IDs de los miembros del grupo
    private Set<Long> adminsIds; // IDs de los administradores del grupo
}
