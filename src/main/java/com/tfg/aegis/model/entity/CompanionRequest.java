package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.CompanionRequestEnums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "companionRequest")
public class CompanionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Location source;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Location destination;

    private LocalDateTime aproxHour;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private CompanionRequestEnums.RequestStatus state;

    private LocalDateTime creationDate;

    @Column(name = "companion_message", length = 500)
    private String companionMessage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "acompaniante_id")
    private User companion;

    @OneToOne
    @JoinColumn(name = "companion_group_id")
    private Group companionGroup;

    @ManyToOne
    @JoinColumn(name = "creator_tracking_group_id")
    private Group creatorTrackingGroup;

    @ManyToOne
    @JoinColumn(name = "companion_tracking_group_id")
    private Group companionTrackingGroup;
}
