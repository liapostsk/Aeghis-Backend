package com.tfg.aegis.invitation;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;
import com.tfg.aegis.invitation.mapper.InvitationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationMapper mapper;

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    /**
     * Method that creates an Invitation
     *
     * @param groupId groupId Group id
     * @param expiry expiry Optional expiry time (e.g., 30 minutes or 60 minutes, etc.)
     * @return InvitationDto
     */
    public InvitationDto createInvitation(Long groupId, Long expiry) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + groupId));

        String code = generateCode();
        String codeHash = passwordEncoder.encode(code);

        long minutes = (expiry == null) ? 60L : Math.max(1L, expiry); // evita 0 o negativos
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(minutes);

        Invitation invitation = new Invitation();
        invitation.setGroup(group);
        invitation.setExpiresAt(expiresAt);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setCodeHash(codeHash); // Método para generar un código hash único

        Invitation saved = invitationRepository.save(invitation);

        return mapper.toDto(saved, code);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Method that validates an invitation
     * @return true if the invitation is valid, false otherwise
     */
    public Boolean validateInvitation(Long groupId, String code) {
        if (code == null || code.isBlank()) return false;

        String input = code.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        List<Invitation> activeInvites =
                invitationRepository.findActiveByGroupId(groupId, now);
        if (activeInvites.isEmpty()) return false;

        return activeInvites.stream()
                .anyMatch(inv -> passwordEncoder.matches(input, inv.getCodeHash()));
    }
}
