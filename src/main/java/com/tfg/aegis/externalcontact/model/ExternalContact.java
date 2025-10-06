package com.tfg.aegis.externalcontact.model;

import com.tfg.aegis.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "external_contact",
    // Activa la unique si NO quieres duplicar teléfonos externos para el mismo owner:
    uniqueConstraints = @UniqueConstraint(
            name = "uk_ext_owner_phone", columnNames = {"owner_id","phone"}),
    indexes = {
            @Index(name = "ix_en_owner", columnList = "owner_id"),
            @Index(name = "ix_en_phone", columnList = "phone")
    }
)
public class ExternalContact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quién define el contacto de emergencia externo */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ext_owner"))
    private User owner;

    @Column(length = 120, nullable = false)
    private String name;

    /** Guarda en formato E.164 (+34...) para poder “promocionar” si se registra */
    @Column(length = 32, nullable = false)
    private String phone;

    @Column(length = 16)
    private String relation;
}