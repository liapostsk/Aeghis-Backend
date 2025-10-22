package com.tfg.aegis.user;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.emergencycontact.EmergencyContactRepository;
import com.tfg.aegis.emergencycontact.mapper.EmergencyContactMapper;
import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.externalcontact.ExternalContactRepository;
import com.tfg.aegis.externalcontact.mapper.ExternalContactMapper;
import com.tfg.aegis.externalcontact.model.ExternalContact;
import com.tfg.aegis.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.user.mapper.UserMapper;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final ExternalContactRepository externalContactRepository;

    private final UserMapper mapper;
    private final EmergencyContactMapper emergencyContactMapper;
    private final ExternalContactMapper externalContactMapper;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /**
     * Method that gets the current user
     * @param clerkId Clerk ID of the user
     */
    public UserDto getUserByClerkId(String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User with clerkId %s not found".formatted(clerkId)));

        UserDto dto = mapper.toDto(user);

        Set<EmergencyContactDto> contacts = emergencyContactRepository.findByOwnerId(user.getId()).stream()
                .map(emergencyContactMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());

        Set<ExternalContactDto> externalContacts = externalContactRepository.findByOwnerId(user.getId()).stream()
                .map(externalContactMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());

        dto.setEmergencyContacts(contacts);
        dto.setExternalContacts(externalContacts);

        return dto;
    }

    /**
     * Method that gets a User
     * @param id User id
     * @return UserDto
     */
    public UserDto getUser(Long id) {
        // Usamos orElseThrow para lanzar la excepción automáticamente cuando no se encuentra el usuario.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        UserDto dto = mapper.toDto(user);

        Set<EmergencyContactDto> contacts = emergencyContactRepository.findByOwnerId(user.getId()).stream()
                .map(emergencyContactMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());
        dto.setEmergencyContacts(contacts);
        Set<ExternalContactDto> externalContacts = externalContactRepository.findByOwnerId(user.getId()).stream()
                .map(externalContactMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());
        dto.setExternalContacts(externalContacts);

        return dto;
    }

    /**
     * Method that creates a User
     * @body UserDto
     */
    public Long createUser(UserDto userDto) {
        try {
            User user = mapper.toEntity(userDto);

            Set<EmergencyContact> contacts = new HashSet<>();
            if (userDto.getEmergencyContacts() != null) {
                // Por cada contacto de emergencia, coger el numero de teléfono y buscar el usuario asociado
                for (EmergencyContactDto contactDto : userDto.getEmergencyContacts()) {
                    Optional<User> contactUser = userRepository.findById(contactDto.getContactId());
                    log.info("Contact user: {}", contactUser);
                    if (contactUser.isPresent()) {
                        EmergencyContact contact = emergencyContactMapper.toEntity(contactDto);
                        contact.setOwner(user);
                        contact.setContact(contactUser.get());
                        contacts.add(contact);
                    } else {
                        throw new NotFoundException("Emergency Contact User", contactDto.getContactId());
                    }
                }
            }
            user.setEmergencyContacts(contacts);

            Set<ExternalContact> externalContacts = new HashSet<>();
            if (userDto.getExternalContacts() != null) {
                for (ExternalContactDto externalContactDto : userDto.getExternalContacts()) {
                    ExternalContact externalContact = externalContactMapper.toEntity(externalContactDto);
                    externalContact.setOwner(user);
                    externalContacts.add(externalContact);
                }
            }
            user.setExternalContacts(externalContacts);
            User saved = userRepository.save(user);

            return saved.getId();
        } catch (DataIntegrityViolationException ex) {
            // Índices únicos (email/phone) → 409 con mensaje claro
            throw new ConflictException("Email or phone already in use");
        }
    }

    /**
     * Method that updates the info of a User
     * @param id User id
     * @param userDto UserDto
     */
    public void updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        // Actualizamos los campos del usuario
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        user.setEmail(userDto.getEmail());
        user.setVerify(userDto.getVerify());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email or phone already in use");
        }
    }

    /**
     * Method that deletes a User
     * @param id User id
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Method that checks if a User exists by phone number
     * @param phone Phone number
     * @return Long User id or null if not exists
     */
    public Long userExistsByPhone(String phone) {
        return userRepository.findByPhone(phone).map(User::getId).orElse(null);
    }
}
