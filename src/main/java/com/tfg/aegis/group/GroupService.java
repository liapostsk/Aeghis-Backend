package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.invitation.InvitationService;
import com.tfg.aegis.group.mapper.GroupMapper;
import com.tfg.aegis.user.UserService;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.model.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final InvitationService invitationService;

    private final GroupMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);


    /**
     * Method that creates a Group
     *
     * @param groupDto GroupDto
     * @return Group id
     */
    public Long createGroup(GroupDto groupDto) {

        User owner = userRepository.findById(groupDto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + groupDto.getOwnerId()));
        Group group = new Group();
        group.setName(groupDto.getName());
        group.setDescription(groupDto.getDescription());
        group.setOwner(owner);
        group.setImageUrl(null);
        Set<User> members = new HashSet<>();
        members.add(owner);
        group.setMembers(members);
        group.setType(Enums.TypeGroup.CONFIANZA);
        group.setState(Enums.GroupState.PENDIENTE);
        group.setLastModified(LocalDateTime.now());
        return groupRepository.save(group).getId();
    }

    /**
     * Method that retrieves a Group by its id
     */
    public GroupDto getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        return mapper.toDto(group);
    }

    /**
     * Method that allows a user to join a group
     *
     * @param userId  User id
     */
    public Long joinGroup(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Invitation code is required");
        }
        String normalized = code.trim().toUpperCase();

        // 4) Validación de invitación
        GroupDto valid = invitationService.validateInvitation(normalized);

        Group group = groupRepository.findById(valid.getId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + valid.getId()));

        // 5) Añadir miembro
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }
        group.getMembers().add(user);

        // 6) Activar si estaba pendiente
        if (group.getState() == Enums.GroupState.PENDIENTE) {
            group.setState(Enums.GroupState.ACTIVO);
        }
        log.info("User {} joined group {}", user.getId(), group);
        groupRepository.save(group);

        return group.getId();
    }

    /**
     * Method that retrieves all groups of a specific type that the authenticated user belongs to
     *
     * @param type Type of group
     * @return List of GroupDto
     */
    public List<GroupDto> getAllMyGroupsByType(Enums.TypeGroup type) {
        if (type == null) {
            throw new IllegalArgumentException("Group type cannot be null");
        }

        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDto userDto = userService.getUserByClerkId(clerkId);

        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<GroupDto> listGroups = groupRepository.findByTypeAndMembers_Id(type, userDto.getId())
                .stream()
                .map(mapper::toDto)
                .toList();

        log.info("List of groups {}", listGroups);

        return listGroups;
    }

    /**
     * Method that allows a user to exit a group
     *
     * @param groupId Group id
     * @param userId  User id
     */
    public void exitGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 1) Comprobar si el usuario es miembro del grupo
        if (group.getMembers() == null || !group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        // 2) Eliminar al usuario del grupo
        group.getMembers().remove(user);

        // 3) Si el grupo se queda sin miembros, cerrarlo
        if (group.getMembers().isEmpty()) {
            group.setState(Enums.GroupState.CERRADO);
        }

        groupRepository.save(group);
    }
}
