package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.repository.EmergencyContactRepository;
import com.tfg.aegis.model.mapper.EmergencyContactMapper;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.repository.ExternalContactRepository;
import com.tfg.aegis.model.mapper.ExternalContactMapper;
import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.model.mapper.UserMapper;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.enums.UserEnums;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.dto.GroupDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper mapper;
    @Mock
    private EmergencyContactRepository emergencyContactRepository;
    @Mock
    private ExternalContactRepository externalContactRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private EmergencyContactMapper emergencyContactMapper;
    @Mock
    private ExternalContactMapper externalContactMapper;
    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_whenExists_returnsDto() {
        Long id = 1L;
        User entity = new User();
        entity.setId(id);

        User contact = new User();
        contact.setId(2L);

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        Set<EmergencyContact> emergencyContacts = new HashSet<>();

        EmergencyContact ec1 = new EmergencyContact();
        ec1.setId(1L);
        ec1.setOwner(entity);
        ec1.setContact(contact);
        emergencyContacts.add(ec1);

        when(emergencyContactRepository.findByOwnerId(id)).thenReturn(emergencyContacts);

        EmergencyContactDto ecDto = new EmergencyContactDto();
        ecDto.setId(1L);
        when(emergencyContactMapper.toDto(any(EmergencyContact.class))).thenReturn(ecDto);

        Set<ExternalContact> externalContacts = new HashSet<>();

        ExternalContact ext1 = new ExternalContact();
        ext1.setId(1L);
        ext1.setOwner(entity);
        ext1.setPhone("123456789");
        externalContacts.add(ext1);

        when(externalContactRepository.findByOwnerId(id)).thenReturn(externalContacts);

        ExternalContactDto extDto = new ExternalContactDto();
        extDto.setId(1L);
        when(externalContactMapper.toDto(any(ExternalContact.class))).thenReturn(extDto);

        UserDto dto = new UserDto();
        when(mapper.toDto(entity)).thenReturn(dto);

        UserDto result = userService.getUser(id);

        assertSame(dto, result);
    }

    @Test
    void getUser_whenNotExists_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUser(99L));
    }

    @Test
    void createUser_ok_returnsId() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPhone("123456789");
        dto.setDateOfBirth(new Date());

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(1L, id);
        verify(userRepository).save(toSave);
    }

    @Test
    void createUser_uniqueConstraint_throwsConflict() {
        UserDto dto = new UserDto();
        dto.setEmail("dup@example.com");
        dto.setPhone("123");

        when(mapper.toEntity(dto)).thenReturn(new User());
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThrows(ConflictException.class, () -> userService.createUser(dto));
    }

    @Test
    void updateUser_ok_savesWithNewFields() {
        Long id = 5L;
        User existing = new User();
        existing.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        UserDto dto = new UserDto();
        dto.setName("Updated");
        dto.setEmail("upd@example.com");
        dto.setPhone("987654321");
        dto.setVerify(UserEnums.VerificationStatus.PENDING);
        dto.setDateOfBirth(new Date());

        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateUser(id, dto);

        assertEquals("Updated", existing.getName());
        assertEquals("upd@example.com", existing.getEmail());
        assertEquals("987654321", existing.getPhone());
        assertEquals(UserEnums.VerificationStatus.PENDING, existing.getVerify());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_notFound_throwsNotFound() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.updateUser(7L, new UserDto()));
    }

    @Test
    void updateUser_uniqueConstraint_throwsConflict() {
        Long id = 8L;
        User existing = new User();
        existing.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing))
                .thenThrow(new DataIntegrityViolationException("unique"));

        assertThrows(ConflictException.class, () -> userService.updateUser(id, new UserDto()));
    }

    @Test
    void deleteUser_ok_deletesById() {
        Long id = 10L;
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound_throwsNotFound() {
        when(userRepository.existsById(11L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(11L));
    }

    /* =========================
     * getUserByClerkId
     * ========================= */
    @Test
    void getUserByClerkId_whenExists_returnsDto() {
        String clerkId = "clerk_123";
        User entity = new User();
        entity.setId(1L);
        entity.setClerkId(clerkId);

        when(userRepository.findByClerkId(clerkId)).thenReturn(Optional.of(entity));

        Set<EmergencyContact> emergencyContacts = new HashSet<>();
        EmergencyContact ec1 = new EmergencyContact();
        ec1.setId(1L);
        emergencyContacts.add(ec1);
        when(emergencyContactRepository.findByOwnerId(1L)).thenReturn(emergencyContacts);

        EmergencyContactDto ecDto = new EmergencyContactDto();
        ecDto.setId(1L);
        when(emergencyContactMapper.toDto(any(EmergencyContact.class))).thenReturn(ecDto);

        Set<ExternalContact> externalContacts = new HashSet<>();
        ExternalContact ext1 = new ExternalContact();
        ext1.setId(1L);
        externalContacts.add(ext1);
        when(externalContactRepository.findByOwnerId(1L)).thenReturn(externalContacts);

        ExternalContactDto extDto = new ExternalContactDto();
        extDto.setId(1L);
        when(externalContactMapper.toDto(any(ExternalContact.class))).thenReturn(extDto);

        List<Group> groups = new ArrayList<>();
        Group g1 = new Group();
        g1.setId(1L);
        groups.add(g1);
        when(groupRepository.findByMembers_Id(1L)).thenReturn(groups);

        GroupDto groupDto = new GroupDto();
        groupDto.setId(1L);
        when(groupMapper.toDto(any(Group.class))).thenReturn(groupDto);

        UserDto dto = new UserDto();
        when(mapper.toDto(entity)).thenReturn(dto);

        UserDto result = userService.getUserByClerkId(clerkId);

        assertNotNull(result);
        assertNotNull(result.getEmergencyContacts());
        assertNotNull(result.getExternalContacts());
        assertNotNull(result.getGroups());
        assertEquals(1, result.getEmergencyContacts().size());
        assertEquals(1, result.getExternalContacts().size());
        assertEquals(1, result.getGroups().size());
    }

    @Test
    void getUserByClerkId_whenNotExists_throwsNotFound() {
        when(userRepository.findByClerkId("invalid_clerk")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserByClerkId("invalid_clerk"));
    }

    /* =========================
     * createUser - casos adicionales
     * ========================= */
    @Test
    void createUser_withEmergencyContacts_ok() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPhone("123456789");

        EmergencyContactDto ecDto = new EmergencyContactDto();
        ecDto.setContactId(2L);
        ecDto.setRelation("Friend");
        dto.setEmergencyContacts(Set.of(ecDto));

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        User contactUser = new User();
        contactUser.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(contactUser));

        EmergencyContact ec = new EmergencyContact();
        when(emergencyContactMapper.toEntity(ecDto)).thenReturn(ec);

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(1L, id);
        verify(userRepository).save(toSave);
    }

    @Test
    void createUser_emergencyContactNotFound_throwsNotFound() {
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        EmergencyContactDto ecDto = new EmergencyContactDto();
        ecDto.setContactId(999L);
        dto.setEmergencyContacts(Set.of(ecDto));

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUser_withExternalContacts_ok() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPhone("123456789");

        ExternalContactDto extDto = new ExternalContactDto();
        extDto.setName("External");
        extDto.setPhone("987654321");
        dto.setExternalContacts(Set.of(extDto));

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        ExternalContact ext = new ExternalContact();
        when(externalContactMapper.toEntity(extDto)).thenReturn(ext);

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(1L, id);
        verify(userRepository).save(toSave);
    }

    @Test
    void createUser_asAdmin_setsAdminRole() {
        UserDto dto = new UserDto();
        dto.setName("Admin User");
        dto.setEmail("admin@example.com");
        dto.setPhone("123456789");

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        // Configurar el valor de admin.emails
        ReflectionTestUtils.setField(userService, "adminEmailsStr", "admin@example.com,other@example.com");

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(1L, id);
        assertEquals(UserEnums.TypeRole.ADMIN, toSave.getRole());
        assertEquals(UserEnums.VerificationStatus.PENDING, toSave.getVerify());
    }

    @Test
    void createUser_asRegularUser_setsUserRole() {
        UserDto dto = new UserDto();
        dto.setName("Regular User");
        dto.setEmail("user@example.com");
        dto.setPhone("123456789");

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        ReflectionTestUtils.setField(userService, "adminEmailsStr", "admin@example.com");

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(1L, id);
        assertEquals(UserEnums.TypeRole.USER, toSave.getRole());
    }

    @Test
    void createUser_adminEmailsNull_defaultsToUser() {
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        User toSave = new User();
        when(mapper.toEntity(dto)).thenReturn(toSave);

        ReflectionTestUtils.setField(userService, "adminEmailsStr", null);

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(toSave)).thenReturn(saved);

        Long id = userService.createUser(dto);

        assertEquals(UserEnums.TypeRole.USER, toSave.getRole());
    }

    /* =========================
     * userExistsByPhone
     * ========================= */
    @Test
    void userExistsByPhone_whenExists_returnsId() {
        String phone = "123456789";
        User user = new User();
        user.setId(5L);
        user.setPhone(phone);

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        Long result = userService.userExistsByPhone(phone);

        assertEquals(5L, result);
    }

    @Test
    void userExistsByPhone_whenNotExists_returnsNull() {
        when(userRepository.findByPhone("999999999")).thenReturn(Optional.empty());

        Long result = userService.userExistsByPhone("999999999");

        assertNull(result);
    }

    /* =========================
     * addPhotoToUser
     * ========================= */
    @Test
    void addPhotoToUser_ok_updatesImage() {
        Long id = 1L;
        String photo = "base64photo";

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.addPhotoToUser(id, photo);

        assertEquals(photo, user.getImage());
        verify(userRepository).save(user);
    }

    @Test
    void addPhotoToUser_replacesExistingPhoto() {
        Long id = 1L;
        String oldPhoto = "old_photo";
        String newPhoto = "new_photo";

        User user = new User();
        user.setId(id);
        user.setImage(oldPhoto);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.addPhotoToUser(id, newPhoto);

        assertEquals(newPhoto, user.getImage());
        verify(userRepository).save(user);
    }

    @Test
    void addPhotoToUser_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.addPhotoToUser(99L, "photo"));
    }

    /* =========================
     * getUnverifiedUsers
     * ========================= */
    @Test
    void getUnverifiedUsers_returnsListOfPendingUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setVerify(UserEnums.VerificationStatus.PENDING);

        User user2 = new User();
        user2.setId(2L);
        user2.setVerify(UserEnums.VerificationStatus.PENDING);

        when(userRepository.findByVerify(UserEnums.VerificationStatus.PENDING))
                .thenReturn(List.of(user1, user2));

        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        UserDto dto2 = new UserDto();
        dto2.setId(2L);

        when(mapper.toDto(user1)).thenReturn(dto1);
        when(mapper.toDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.getUnverifiedUsers();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getUnverifiedUsers_whenNoPending_returnsEmptyList() {
        when(userRepository.findByVerify(UserEnums.VerificationStatus.PENDING))
                .thenReturn(List.of());

        List<UserDto> result = userService.getUnverifiedUsers();

        assertTrue(result.isEmpty());
    }

    /* =========================
     * verifyUser
     * ========================= */
    @Test
    void verifyUser_ok_updatesStatus() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setVerify(UserEnums.VerificationStatus.PENDING);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.verifyUser(id, UserEnums.VerificationStatus.VERIFIED);

        assertEquals(UserEnums.VerificationStatus.VERIFIED, user.getVerify());
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_toRejected_ok() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setVerify(UserEnums.VerificationStatus.PENDING);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.verifyUser(id, UserEnums.VerificationStatus.REJECTED);

        assertEquals(UserEnums.VerificationStatus.REJECTED, user.getVerify());
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> userService.verifyUser(99L, UserEnums.VerificationStatus.VERIFIED));
    }
}
