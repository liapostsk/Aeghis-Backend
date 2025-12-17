package com.tfg.aegis.model.entity;

import com.tfg.aegis.model.enums.EmergencyContactEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(
    name = "emergency_contact",
    // NO duplicar teléfonos externos para el mismo owner
    uniqueConstraints = @UniqueConstraint(
            name = "uk_emc_owner_contact", columnNames = {"owner_id","contact_id"}),
    indexes = {
            @Index(name = "ik_emc_owner", columnList = "owner_id"),
            @Index(name = "ix_emc_contact", columnList = "contact_id")
    }
)
public class EmergencyContact {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Quién define el contacto de emergencia */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_emc_owner"))
  private User owner;

  /** A quién pone como contacto (otro usuario de la app) */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_emc_contact"))
  private User contact;

  private String relation;

  /** Estado del consentimiento / flujo */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmergencyContactEnum.Status status;

  @PrePersist
  @PreUpdate
  private void validate() {
    if (owner != null && contact != null && Objects.equals(owner.getId(), contact.getId())) {
        throw new IllegalStateException("A user cannot add themselves as emergency contact");
    }
  }
}
