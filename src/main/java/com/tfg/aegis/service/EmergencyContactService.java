package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ApiException;
import com.tfg.aegis.model.mapper.EmergencyContactMapper;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.model.enums.emergencyContactEnum;
import com.tfg.aegis.repository.ExternalContactRepository;
import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.repository.EmergencyContactRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ExternalContactRepository externalContactRepository;
    private final EmergencyContactMapper emergencyContactMapper;

    /**
     * Retrieves an emergency contact by its ID, ensuring that the contact belongs to the current user.
     *
     * @param contactId the ID of the emergency contact to retrieve
     * @return the EmergencyContact object if found and owned by the current user
     * @throws NotFoundException if the contact does not exist
     * @throws AccessDeniedException if the contact does not belong to the current user
     */
    private EmergencyContact getOwnedContactOrThrow(Long contactId) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto userDto = userService.getUserByClerkId(clerkId);
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new NotFoundException("Emergency Contact not found"));
        if (!contact.getOwner().getId().equals(userDto.getId())) {
            throw new AccessDeniedException("You do not own this SafeLocation");
        }
        return contact;
    }

    /**
     * Adds a new emergency contact for the current user.
     *
     * @param emergencyContactDto the emergency contact data transfer object
     * @return the ID of the newly created emergency contact
     * @throws NotFoundException if the current user is not found
     */
    public EmergencyContactDto addEmergencyContactForCurrentUser(EmergencyContactDto emergencyContactDto) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User owner = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User not found with clerkId: " + clerkId)); // Current user

        // 1) No es usuario de la app
        User contact = userRepository.findById(emergencyContactDto.getContactId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not at the app", "USER_NOT_IN_APP", "El usuario con id " + emergencyContactDto.getContactId() + " no está en la app"));

        // 2) El contacto de emergencia eres tú mismo
        if (contact.getPhone().equals(owner.getPhone())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Same user", "SELF_ADD", "No puedes agregarte a ti mismo");
        }

        EmergencyContact entity = emergencyContactMapper.toEntity(emergencyContactDto);
        entity.setOwner(owner);
        entity.setContact(contact);

        emergencyContactRepository.save(entity);
        return emergencyContactMapper.toDto(entity);
    }

    /**
     * Edits an existing emergency contact for the current user.
     *
     * @param id the ID of the emergency contact to edit
     * @param emergencyContactDto the updated emergency contact data transfer object
     * @throws NotFoundException if the contact does not exist
     * @throws AccessDeniedException if the contact does not belong to the current user
     */
    public void editEmergencyContact(Long id, EmergencyContactDto emergencyContactDto) {
        EmergencyContact contact = getOwnedContactOrThrow(id); // Verify ownership
        // Update fields
        contact.setRelation(emergencyContactDto.getRelation());
        emergencyContactRepository.save(contact);
    }

    /**
     * Deletes an emergency contact for the current user.
     *
     * @param id the ID of the emergency contact to delete
     * @throws NotFoundException if the contact does not exist
     * @throws AccessDeniedException if the contact does not belong to the current user
     * @throws IllegalStateException if the contact was not deleted successfully
     */
    public void deleteEmergencyContactForCurrentUser(Long id) {
        boolean existed = emergencyContactRepository.existsById(id);
        emergencyContactRepository.deleteById(id);
        if (existed && emergencyContactRepository.existsById(id)) {
            throw new IllegalStateException("No se borró el EmergencyContact id=" + id);
        }
    }

    public Long requestPromoteExternalToEmergencyContact(ExternalContact externalContact, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        // En este metodo se coge un external contact del usuaro y se convierte en un emergency contact
        User contact = userRepository.findByPhone(externalContact.getPhone())
                .orElseThrow(() -> new NotFoundException("User with phone %s not found".formatted(externalContact.getPhone())));

        EmergencyContact emergencyContact = new EmergencyContact();
        emergencyContact.setOwner(owner);
        emergencyContact.setContact(contact);
        emergencyContact.setRelation(externalContact.getRelation());
        emergencyContact.setStatus(emergencyContactEnum.Status.PENDING);

        EmergencyContact saved = emergencyContactRepository.save(emergencyContact);

        // Elimino el contacto externo
        externalContactRepository.delete(externalContact);

        return saved.getId();
    }

    public void acceptEmergencyContact(Long emergencyContactId) {
        EmergencyContact contact = emergencyContactRepository.findById(emergencyContactId)
                .orElseThrow(() -> new NotFoundException("EmergencyContact", emergencyContactId));
        if (contact.getStatus() == emergencyContactEnum.Status.PENDING) {
            contact.setStatus(emergencyContactEnum.Status.ACCEPTED);
            emergencyContactRepository.save(contact);
        }
    }
}
