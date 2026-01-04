package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.repository.EmergencyContactRepository;
import com.tfg.aegis.model.mapper.EmergencyContactMapper;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.repository.ExternalContactRepository;
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
