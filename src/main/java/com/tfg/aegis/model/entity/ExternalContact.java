package com.tfg.aegis.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "external_contact",
    uniqueConstraints = @UniqueConstraint(
            name = "uk_ext_owner_phone", columnNames = {"owner_id","phone"})
    )
public class ExternalContact extends Person {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ext_owner"))
    private User owner;

    @Column(length = 16)
    private String relation;
}