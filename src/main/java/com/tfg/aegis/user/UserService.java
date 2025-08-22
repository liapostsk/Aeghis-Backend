package com.tfg.aegis.user;

import common.exception.*;
import com.tfg.aegis.user.mapper.UserMapper;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    /**
     * Method that gets the current user
     * @param clerkId Clerk ID of the user
     */
    public UserDto getUserByClerkId(String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User with clerkId %s not found".formatted(clerkId)));
        return mapper.toDto(user);
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
        return mapper.toDto(user);
    }

    /**
     * Method that creates a User
     * @body UserDto
     */
    public Long createUser(UserDto userDto) {
        try {
            User saved = userRepository.save(mapper.toEntity(userDto));
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
}
