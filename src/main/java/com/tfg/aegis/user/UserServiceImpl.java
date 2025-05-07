package com.tfg.aegis.user;

import com.tfg.aegis.exception.InternalServerException;
import com.tfg.aegis.exception.user.ResourceNotFoundException;
import com.tfg.aegis.exception.user.UserCreationException;
import com.tfg.aegis.exception.user.UserNotFoundException;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class    UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with clerkId: " + clerkId));
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(Long id) {
        // Usamos orElseThrow para lanzar la excepción automáticamente cuando no se encuentra el usuario.
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)); // Lanza la UserNotFoundException automáticamente
    }

    /**
     * {@inheritDoc}
     */
    public Long createUser(UserDto userDto) {
        try {
            if (userRepository.existsByPhone(userDto.getPhone())) {
                throw new UserCreationException("A user with this phone already exists.");
            }

            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new UserCreationException("A user with this email already exists.");
            }

            User user = new User();
            user.setDateOfBirth(userDto.getDateOfBirth());
            user.setName(userDto.getName());
            user.setPhone(userDto.getPhone());
            user.setEmail(userDto.getEmail());
            user.setVerify(userDto.getVerify());
            user.setClerkId(userDto.getClerkId());

            User savedUser = userRepository.save(user);
            return savedUser.getId();

        } catch (UserCreationException e) {
            throw e; // ya está controlada como 400
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while creating user.", e); // lanza 500
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateUser(Long id, UserDto userDto) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            user.setDateOfBirth(userDto.getDateOfBirth());
            user.setName(userDto.getName());
            user.setPhone(userDto.getPhone());
            user.setEmail(userDto.getEmail());
            user.setVerify(userDto.getVerify());

            userRepository.save(user);
        } catch (UserNotFoundException e) {
            throw e; // Ya controlada como 404
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while updating user with id: " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
            userRepository.delete(user);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while deleting user with id: " + id, e);
        }
    }
}
