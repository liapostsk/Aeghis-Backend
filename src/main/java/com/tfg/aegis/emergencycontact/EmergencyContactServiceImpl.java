package com.tfg.aegis.emergencycontact;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.exception.user.ResourceNotFoundException;
import com.tfg.aegis.mapper.EmergencyContactMapper;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.UserServiceImpl;
import com.tfg.aegis.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactRepository repository;

    private final UserRepository userRepository;

    private final UserServiceImpl userServiceImpl;

    public EmergencyContactServiceImpl(EmergencyContactRepository repository, UserRepository userRepository, UserServiceImpl userServiceImpl) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
    }

    private EmergencyContact getOwnedContactOrThrow(Long contactId) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userServiceImpl.getUserByClerkId(clerkId);
        EmergencyContact contact = repository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency Contact not found"));
        if (!contact.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this SafeLocation");
        }
        return contact;
    }

    @Override
    public void addEmergencyContactForCurrentUser(EmergencyContactDto emergencyContactDto) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with clerkId: " + clerkId));
        EmergencyContact contact = EmergencyContactMapper.toEntity(emergencyContactDto, user);
        repository.save(contact);
    }

    @Override
    public void editEmergencyContact(Long id, EmergencyContactDto emergencyContactDto) {
        EmergencyContact contact = getOwnedContactOrThrow(id);
        contact.setName(emergencyContactDto.getName());
        contact.setPhone(emergencyContactDto.getPhone());
        contact.setRelation(emergencyContactDto.getRelation());
        contact.setConfirmed(emergencyContactDto.isConfirmed());
        repository.save(contact);
    }

    @Override
    public void deleteEmergencyContactForCurrentUser(Long id) {
        EmergencyContact contact = getOwnedContactOrThrow(id);
        repository.delete(contact);
        if (repository.existsById(id)) {
            throw new ResourceNotFoundException("Emergency Contact not found after deletion");
        }
    }
}
