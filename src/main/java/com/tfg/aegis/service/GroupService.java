package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.common.exception.UnauthorizedException;
import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.repository.InvitationRepository;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.dto.UserDto;
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
    private final InvitationRepository invitationRepository;
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
        Group group = mapper.toEntity(groupDto);

        group.setOwner(owner);
        Set<User> members = new HashSet<>();
        members.add(owner);
        if (groupDto.getMembersIds() != null) {
            for (Long memberId : groupDto.getMembersIds()) {
                if (!memberId.equals(owner.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + memberId));
                    members.add(member);
                }
            }
        }
        group.setMembers(members);
        group.setState(GroupEnums.GroupState.PENDIENTE);
        group.setLastModified(LocalDateTime.now());
        // companionRequestId puede ser nulo al crear el grupo

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
        if (group.getState() == GroupEnums.GroupState.PENDIENTE) {
            group.setState(GroupEnums.GroupState.ACTIVO);
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
    public List<GroupDto> getAllMyGroupsByType(GroupEnums.TypeGroup type) {
        if (type == null) {
            throw new IllegalArgumentException("Group type cannot be null");
        }

        UserDto userDto = getCurrentUser();

        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();

        List<GroupDto> listGroups = groupRepository
                .findByTypeAndMemberNotExpired(type, userDto.getId(), now)
                .stream()
                .map(mapper::toDto)
                .toList();

        log.info("List of groups {}", listGroups);

        return listGroups;
    }

    /**
     * Method that retrieves all groups that the authenticated user belongs to
     *
     * @return List of GroupDto
     */
    public List<GroupDto> getAllMyGroups() {
        UserDto userDto = getCurrentUser();
        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        List<GroupDto> listGroups = groupRepository.findByMembers_Id(userDto.getId())
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
    public GroupDto exitGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 1) Comprobar si el usuario es miembro del grupo
        if (group.getMembers() == null || !group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        // 2) Si el usuario es admin del grupo y no hay mas admins, asignar otro admin
        if (group.getAdmins() != null && group.getAdmins().contains(user)) {
            group.getAdmins().remove(user);
            if (group.getAdmins().isEmpty() && !group.getMembers().isEmpty()) {
                log.info("Assigning new admin in group {} as the last admin {} is leaving", group.getId(), user.getId());
                User newAdmin = group.getMembers().iterator().next();
                group.getAdmins().add(newAdmin);
                log.info("New admin assigned: {} in group {}", newAdmin.getId(), group.getId());
            }
        }
        log.info("User {} is leaving group {}", user.getId(), group.getId());
        // 3) Eliminar al usuario del grupo
        group.getMembers().remove(user);

        // 4) Si el grupo se queda sin miembros, cerrarlo
        if (group.getMembers().isEmpty()) {
            group.setState(GroupEnums.GroupState.CERRADO);
        }

        groupRepository.save(group);

        return mapper.toDto(group);
    }

    /**
     * Method that edits a group by its id
     *
     * @param groupId  Group id
     * @param groupDto GroupDto
     * @return GroupDto
     */
    public GroupDto editGroup(Long groupId, GroupDto groupDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        group.setName(groupDto.getName());
        group.setDescription(groupDto.getDescription());
        group.setLastModified(LocalDateTime.now());
        group.setImageUrl(groupDto.getImageUrl());
        return mapper.toDto(groupRepository.save(group));
    }

    /**
     * Method that deletes a group by its id
     *
     * @param groupId Group id
     */
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        // Si solo queda un miembro, eliminar el grupo
        if (group.getMembers() != null && group.getMembers().size() > 1) {
            throw new IllegalArgumentException("Group cannot be deleted as it has more than one member");
        }
        group.getAdmins().clear();

        invitationRepository.deleteByGroupId(groupId);
        log.info("Deleting group {}", group.getId());
        groupRepository.delete(group);
    }

    /**
     * Method that adds a member to a group
     *
     * @param groupId Group id
     * @param userId  User id
     * @return GroupDto
     */
    public GroupDto addMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }

        boolean alreadyMember = group.getMembers().stream().anyMatch(u -> u.getId().equals(userId));
        if (alreadyMember) {
            return mapper.toDto(group);
        }

        if (group.getState() == GroupEnums.GroupState.CERRADO) {
            throw new ConflictException("Group is closed; cannot add members");
        }
        group.getMembers().add(user);
        log.info("User {} added to group {}", user.getId(), group.getId());
        group.setLastModified(LocalDateTime.now());

        Group saved = groupRepository.save(group);

        // notificaciones firebase podrían ir aquí

        return mapper.toDto(saved);
    }

    /**
     * Method that removes a member from a group
     *
     * @param groupId Group id
     * @param userId  User id
     */
    public void removeMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found with id: " + groupId));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (group.getMembers() == null) group.setMembers(new HashSet<>());
        if (group.getAdmins() == null)  group.setAdmins(new HashSet<>());

        if (!containsById(group.getMembers(), userId)) {
            throw new ConflictException("User is not a member of the group");
        }
        if (group.getState() == GroupEnums.GroupState.CERRADO) {
            throw new ConflictException("Group is closed; cannot remove members");
        }

        Long currentUserId = getCurrentUser().getId();
        boolean selfRemoval   = currentUserId != null && currentUserId.equals(userId);
        boolean currentIsAdmin = containsById(group.getAdmins(), currentUserId);

        if (!selfRemoval && !currentIsAdmin) {
            throw new UnauthorizedException("Only admins can remove other members");
        }

        // Eliminar de admins (si lo era) y de miembros
        removeById(group.getAdmins(), userId);
        removeById(group.getMembers(), userId);

        // Si no quedan miembros → cerrar o borrar
        if (group.getMembers().isEmpty()) {
            if (GroupEnums.TypeGroup.TEMPORAL.equals(group.getType())) {
                group.setState(GroupEnums.GroupState.CERRADO);
                group.setLastModified(LocalDateTime.now());
                groupRepository.save(group);
                log.info("Group {} closed (no members left)", groupId);
            } else {
                log.info("Group {} deleted (no members left)", groupId);
                groupRepository.delete(group);
            }
            return; // importante: no continuar tras cerrar/borrar
        }

        if (group.getAdmins().isEmpty()) {
            // Elegimos el primer miembro restante como nuevo admin
            User newAdmin = group.getMembers().iterator().next();
            group.getAdmins().add(newAdmin);
            log.info("Assigned user {} as new admin in group {} to preserve at least one admin",
                    newAdmin.getId(), groupId);
        }

        group.setLastModified(LocalDateTime.now());
        groupRepository.save(group);
        log.info("User {} removed from group {}", userId, groupId);
    }

    /**
     * Method that promotes a member to admin
     *
     * @param groupId Group id
     * @param userId  User id
     * @return GroupDto
     */
    public GroupDto promoteToAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (group.getMembers() == null || !group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        if (group.getAdmins() == null) {
            group.setAdmins(new HashSet<>());
        }
        group.getAdmins().add(user);
        log.info("User {} promoted to admin in group {}", user.getId(), group.getId());
        return mapper.toDto(groupRepository.save(group));
    }

    /**
     * Method that demotes an admin to member
     *
     * @param groupId Group id
     * @param userId  User id
     * @return GroupDto
     */
    public GroupDto demoteAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (group.getAdmins() == null || !group.getAdmins().contains(user)) {
            throw new IllegalArgumentException("User is not an admin of the group");
        }

        group.getAdmins().remove(user);
        log.info("User {} demoted from admin in group {}", user.getId(), group.getId());
        return mapper.toDto(groupRepository.save(group));
    }

    /**
     * Method that adds a photo to a group
     *
     * @param groupId Group id
     * @param photo   Photo in Base64
     */
    public void addPhotoToGroup(Long groupId, String photo) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        group.setImageUrl(photo);
        groupRepository.save(group);
    }

    /* ===================== Helpers reutilizables ===================== */

    private static boolean containsById(Set<User> users, Long userId) {
        if (users == null || users.isEmpty()) return false;
        for (User u : users) {
            if (u != null && u.getId() != null && u.getId().equals(userId)) return true;
        }
        return false;
    }

    private static void removeById(Set<User> users, Long userId) {
        if (users == null || users.isEmpty()) return;
        users.removeIf(u -> u != null && u.getId() != null && u.getId().equals(userId));
    }

    private UserDto getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByClerkId(clerkId);
    }
}
