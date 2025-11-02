package com.tfg.aegis.person.externalcontact.model;

import com.tfg.aegis.person.model.Person;
import com.tfg.aegis.person.user.model.User;
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
            name = "uk_ext_owner_phone", columnNames = {"owner_id","phone"})
    )
public class ExternalContact extends Person {

    /** Quién define el contacto de emergencia externo */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ext_owner"))
    private User owner;

    @Column(length = 16)
    private String relation;
}