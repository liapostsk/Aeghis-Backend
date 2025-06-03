package com.tfg.aegis.emergencycontact.model;

import com.tfg.aegis.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "emergency_contact")
public class EmergencyContact {
  @Id @GeneratedValue
  private Long id;

  @ManyToOne
  @JoinColumn(name = "owner", nullable = false)
  private User owner;

  // Contacto registrado (puede ser null si es no registrado)
  @ManyToOne
  @JoinColumn(name = "emergencyContact")
  private User emergencyContact;

  // Campos para contacto no registrado, en caso que contact_id sea null
  // si no lleban nullable false, se pueden dejar vacios
  private String name;
  private String phone;
  private String relation;

  private boolean confirmed; // por si quieres una validación
}
