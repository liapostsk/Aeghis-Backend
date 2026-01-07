package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.repository.*;
import com.tfg.aegis.model.mapper.EmergencyContactMapper;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.model.mapper.ExternalContactMapper;
import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.model.mapper.UserMapper;
import com.tfg.aegis.model.enums.UserEnums;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final ExternalContactRepository externalContactRepository;
    private final GroupRepository groupRepository;
    private final CompanionRequestRepository companionRequestRepository;
    private final PersonRepository personRepository;

    private final UserMapper mapper;
    private final EmergencyContactMapper emergencyContactMapper;
    private final ExternalContactMapper externalContactMapper;
    private final GroupMapper groupMapper;

    @Value("${app.admin.emails}")
    private String adminEmailsStr;

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

        Set<GroupDto> groups = groupRepository.findByMembers_Id(user.getId()).stream()
                .map(groupMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());

        dto.setGroups(groups);
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

        Set<GroupDto> groups = groupRepository.findByMembers_Id(user.getId()).stream()
                .map(groupMapper::toDto)
                .collect(java.util.stream.Collectors.toSet());

        dto.setGroups(groups);

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
            if (userDto.getPhone() != null) {
                user.setPhone(userDto.getPhone());
            }

            log.info("Admin emails loaded: {}", adminEmailsStr);
            boolean isAdmin = false;
            if (adminEmailsStr != null && userDto.getEmail() != null) {
                List<String> adminEmails = Arrays.stream(adminEmailsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(String::toLowerCase)
                        .toList();

                if (adminEmails.contains(userDto.getEmail().toLowerCase())) {

                    isAdmin = true;
                    log.info("User {} marcado como ADMIN por email", userDto.getEmail());
                }
            }
            user.setRole(isAdmin ? UserEnums.TypeRole.ADMIN : UserEnums.TypeRole.USER);
            user.setVerify(UserEnums.VerificationStatus.NO_REQUEST);

            user.setCompanionRequestsAccepted(new HashSet<>());
            user.setCompanionRequestsCreated(new HashSet<>());

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        Random random = new Random();

        // 1) Grupos donde el usuario es OWNER -> transferir ownership random o borrar el grupo si queda vacío
        List<Group> ownedGroups = groupRepository.findByOwnerId(id);

        for (Group group : ownedGroups) {
            // quitar al usuario de admins/members
            group.getAdmins().remove(user);
            group.getMembers().remove(user);

            // si se queda vacío, borrar el grupo
            if (group.getMembers().isEmpty()) {
                groupRepository.delete(group);
                continue;
            }

            // transferir owner a un miembro random que queda
            List<User> candidates = new ArrayList<>(group.getMembers());
            User newOwner = candidates.get(random.nextInt(candidates.size()));
            group.setOwner(newOwner);

            // asegurar que el nuevo owner es admin
            group.getAdmins().add(newOwner);

            groupRepository.save(group);
        }

        // 2) Grupos donde el usuario es MEMBER/ADMIN (sin query extra y sin flush)
        Set<Group> memberGroups = new HashSet<>(user.getGroups());

        for (Group group : memberGroups) {
            // por si alguno ya se procesó arriba o no lo contiene
            if (!group.getMembers().contains(user)) continue;

            // quitar de admins; si se queda sin admins, promover a otro miembro
            if (group.getAdmins().contains(user)) {
                group.getAdmins().remove(user);

                if (group.getAdmins().isEmpty() && !group.getMembers().isEmpty()) {
                    group.getMembers().stream()
                            .filter(m -> !m.getId().equals(id))
                            .findFirst()
                            .ifPresent(group.getAdmins()::add);
                }
            }

            // quitar de members
            group.getMembers().remove(user);

            // si el grupo queda vacío, borrarlo; si no, guardar
            if (group.getMembers().isEmpty()) {
                groupRepository.delete(group);
            } else {
                groupRepository.save(group);
            }
        }

        // 3) EmergencyContact donde era "contact"
        emergencyContactRepository.deleteByContactId(id);

        // 4) CompanionRequest donde era companion -> companion null + state CREATED (+ romper companionGroup)
        var requestsWhereWasCompanion = companionRequestRepository.findByCompanion_Id(id);
        for (var req : requestsWhereWasCompanion) {
            req.setCompanion(null);
            req.setCompanionGroup(null);
            req.setState(com.tfg.aegis.model.enums.CompanionRequestEnums.RequestStatus.CREATED);
        }
        companionRequestRepository.saveAll(requestsWhereWasCompanion);

        // 5) Borrar user (cascades borran tokens/locations/contacts/participations/requests creadas, etc.)
        userRepository.delete(user);

        // 6) Borrar también Person (para que no quede huérfano)
        personRepository.deleteById(id);
    }

    /**
     * Method that checks if a User exists by phone number
     * @param phone Phone number
     * @return Long User id or null if not exists
     */
    public Long userExistsByPhone(String phone) {
        return userRepository.findByPhone(phone).map(User::getId).orElse(null);
    }

    /**
     * Method that adds a photo to a User
     * @param id User id
     * @param photo Photo in byte array
     */
    public void addPhotoToUser(Long id, String photo) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
        user.setImage(photo);
        userRepository.save(user);
    }

    /**
     * Method that gets the unverified users
     * @return List of UserDto
     */
    public List<UserDto> getUnverifiedUsers() {
        List<User> users = userRepository.findByVerify(UserEnums.VerificationStatus.PENDING);
        List<UserDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(mapper.toDto(user));
        }
        return dtos;
    }

    /**
     * Method that verifies a user
     * @param id User id
     * @param status Verification status
     */
    public void verifyUser(Long id, UserEnums.VerificationStatus status) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
        user.setVerify(status);
        userRepository.save(user);
    }
}
