package com.tfg.aegis.user;

import com.tfg.aegis.exception.InternalServerException;
import com.tfg.aegis.exception.user.UserCreationException;
import com.tfg.aegis.exception.user.UserNotFoundException;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetUserWhenExists() {
        Long userId = 1L;
        Date dateOfBirth = new Date();
        String name = "John Doe";
        String phone = "123456789";
        String email = "juan.perez@example.com";

        User user = new User();
        user.setId(userId);
        user.setDateOfBirth(dateOfBirth);
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setVerify(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void testGetUserWhenNotExists() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void testCreateUserOk() {
        UserDto userDto = new UserDto();
        userDto.setDateOfBirth(new Date());
        userDto.setName("John Doe");
        userDto.setPhone("123456789");
        userDto.setEmail("juan.perez@example.com");
        userDto.setVerify(true);

        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);

        // Simula que al guardar, el repo devuelve un User con ID asignado
        User savedUser = new User();
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long idNewUser = userService.createUser(userDto);
        assertNotNull(idNewUser);
        assertEquals(1L, idNewUser);
    }

    @Test
    void testCreateUserPhoneAlreadyExists() {
        UserDto userDto = new UserDto();
        userDto.setPhone("123456789");
        userDto.setEmail("john@example.com");

        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(true);

        assertThrows(UserCreationException.class, () -> userService.createUser(userDto));
    }

    @Test
    void testCreateUserEmailAlreadyExists() {
        UserDto userDto = new UserDto();
        userDto.setPhone("123456789");
        userDto.setEmail("john@example.com");

        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(UserCreationException.class, () -> userService.createUser(userDto));
    }

    @Test
    void testCreateUserUnexpectedError() {
        UserDto userDto = new UserDto();
        userDto.setPhone("123456789");
        userDto.setEmail("john@example.com");

        when(userRepository.existsByPhone(userDto.getPhone())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new NullPointerException("DB error"));

        assertThrows(InternalServerException.class, () -> userService.createUser(userDto));
    }

    @Test
    void testUpdateUserOk() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);

        UserDto updatedDto = new UserDto();
        updatedDto.setDateOfBirth(new Date());
        updatedDto.setName("Updated Name");
        updatedDto.setPhone("987654321");
        updatedDto.setEmail("updated@example.com");
        updatedDto.setVerify(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.updateUser(userId, updatedDto);
        verify(userRepository).save(argThat(user ->
                user.getName().equals(updatedDto.getName()) &&
                        user.getPhone().equals(updatedDto.getPhone()) &&
                        user.getEmail().equals(updatedDto.getEmail()) &&
                        user.getVerify().equals(updatedDto.getVerify())
        ));
    }

    @Test
    void testUpdateUserNotFound() {
        Long userId = 2L;
        UserDto dto = new UserDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, dto));
    }

    @Test
    void testDeleteUserOk() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void testDeleteUserNotFound() {
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    }

}
