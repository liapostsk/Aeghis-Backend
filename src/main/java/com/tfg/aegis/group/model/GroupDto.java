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
    private String imageUrl;
    private TypeGroup type;
    private GroupState state;
    private LocalDateTime createdAt;
    private LocalDateTime expirationDate;
    private LocalDateTime lastModified;
    private Long ownerId;
    private Set<Long> membersIds;
    private Set<Long> adminsIds;
    private Long companionRequestId;
    // soporte de tracking de grupos
    // private List<Long> trackingRequestsIds;
}
