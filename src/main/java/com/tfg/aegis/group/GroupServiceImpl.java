package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.invitation.InvitationService;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final InvitationService invitationService;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, InvitationService invitationService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.invitationService = invitationService;
    }

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
        group.setState(Enums.GroupState.PENDIENTE);
        group.setLastModified(LocalDateTime.now());
        return groupRepository.save(group).getId();
    }

    /**
     * Method that allows a user to join a group
     *
     * @param groupId Group id
     * @param userId  User id
     */
    @Override
    public void joinGroup(Long groupId, Long userId, String code) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 1) Grupo cerrado => no permite unirse
        if (group.getState() == Enums.GroupState.CERRADO) {
            throw new IllegalArgumentException("Group is closed and does not accept new members");
        }

        // 2) Ya es miembro
        if (group.getMembers() != null && group.getMembers().stream().anyMatch(u -> u.getId().equals(userId))) {
            throw new IllegalArgumentException("User is already a member of the group");
        }

        // 3) Código obligatorio + normalización
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Invitation code is required");
        }
        String normalized = code.trim().toUpperCase();

        // 4) Validación de invitación
        boolean valid = invitationService.validateInvitation(groupId, normalized);
        if (!valid) {
            throw new IllegalArgumentException("Invalid or expired invitation code");
        }

        // 5) Añadir miembro
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }
        group.getMembers().add(user);

        // 6) Activar si estaba pendiente
        if (group.getState() == Enums.GroupState.PENDIENTE) {
            group.setState(Enums.GroupState.ACTIVO);
        }

        groupRepository.save(group);
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
