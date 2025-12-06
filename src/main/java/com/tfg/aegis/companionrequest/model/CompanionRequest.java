package com.tfg.aegis.companionrequest.model;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.person.user.model.User;
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
    private Enums.RequestStatus state;

    private LocalDateTime creationDate;

    // --- Relaciones ---
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
    @JoinColumn(name = "tracking_group_id")
    private Group trackingGroup;

    @OneToOne
    @JoinColumn(name = "trayecto_id")
    private Journey trayecto;
}
