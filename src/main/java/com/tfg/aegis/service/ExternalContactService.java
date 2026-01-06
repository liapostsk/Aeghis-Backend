package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.repository.ExternalContactRepository;
import com.tfg.aegis.model.mapper.ExternalContactMapper;
import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class ExternalContactService {

    private final ExternalContactRepository externalContactRepository;
    private final UserRepository userRepository;
    private final ExternalContactMapper externalContactMapper;
    private final UserService userService;

    /**
     * Creates a new external contact for the current user (owner).
     * @return the ID of the created external contact
     * @throws EntityNotFoundException if the owner user is not found
     * @throws DataIntegrityViolationException if a contact with the same phone already exists for the
     */
    public Long createExternalContactForCurrentUser(ExternalContactDto externalContactDto) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User owner = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User not found with clerkId: " + clerkId));

        ExternalContact external = externalContactMapper.toEntity(externalContactDto);
        external.setOwner(owner);

        owner.getExternalContacts().add(external);

        externalContactRepository.save(external);
        return external.getId();
    }

    /** Actualizar un contacto externo (name/phone/relation) */
    public void editExternalContact(Long id, ExternalContactDto externalContactDto) {
        ExternalContact externalContact = getExternalContactOrThrow(id);

        externalContact.setName(externalContactDto.getName().trim());
        String e164 = normalizeToE164(externalContactDto.getPhone());
        if (!e164.equals(externalContact.getPhone())) {
            externalContactRepository.findFirstByOwnerIdAndPhone(externalContact.getOwner().getId(), e164).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DataIntegrityViolationException("EXTERNAL_DUPLICATE_PHONE");
                }
            });
            externalContact.setPhone(e164);
        }
        externalContact.setRelation(externalContactDto.getRelation());
    }

    /**
     * Deletes an external contact for the current user.
     *
     * @param id the ID of the external contact to delete
     * @throws NotFoundException if the external contact does not exist
     * @throws AccessDeniedException if the external contact does not belong to the current user
     */
    public void deleteExternalContact(Long id) {
        ExternalContact external = getExternalContactOrThrow(id);
        externalContactRepository.delete(external);
    }

    /**
     * Retrieves an emergency contact by its ID, ensuring that the contact belongs to the current user.
     *
     * @param externalId the ID of the emergency contact to retrieve
     * @return the EmergencyContact object if found and owned by the current user
     * @throws NotFoundException if the contact does not exist
     * @throws AccessDeniedException if the contact does not belong to the current user
     */
    private ExternalContact getExternalContactOrThrow(Long externalId) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto userDto = userService.getUserByClerkId(clerkId);
        ExternalContact external = externalContactRepository.findById(externalId)
                .orElseThrow(() -> new NotFoundException("Emergency Contact not found"));
        if (!external.getOwner().getId().equals(userDto.getId())) {
            throw new AccessDeniedException("You do not own this SafeLocation");
        }
        return external;
    }

    private String normalizeToE164(String raw) {
        return raw.replaceAll("\\s+", "");
    }

    public ExternalContactDto getExternalContactForCurrentUser(Long id) {
        ExternalContact externalContact = getExternalContactOrThrow(id);
        return externalContactMapper.toDto(externalContact);
    }
}
