package com.tfg.aegis.user;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.emergencycontact.EmergencyContactRepository;
import com.tfg.aegis.emergencycontact.mapper.EmergencyContactMapper;
import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.person.externalcontact.ExternalContactRepository;
import com.tfg.aegis.person.externalcontact.mapper.ExternalContactMapper;
import com.tfg.aegis.person.externalcontact.model.ExternalContact;
import com.tfg.aegis.person.externalcontact.model.ExternalContactDto;
import com.tfg.aegis.person.user.UserRepository;
import com.tfg.aegis.person.user.UserService;
import com.tfg.aegis.person.user.mapper.UserMapper;
import com.tfg.aegis.person.user.model.User;
import com.tfg.aegis.person.user.model.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private EmergencyContactMapper emergencyContactMapper;
    @Mock
    private ExternalContactMapper externalContactMapper;

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
        dto.setVerify(true);
        dto.setDateOfBirth(new Date());

        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateUser(id, dto);

        assertEquals("Updated", existing.getName());
        assertEquals("upd@example.com", existing.getEmail());
        assertEquals("987654321", existing.getPhone());
        assertEquals(true, existing.getVerify());
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
}
