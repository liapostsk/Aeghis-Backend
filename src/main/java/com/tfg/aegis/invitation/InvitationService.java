package com.tfg.aegis.invitation;

import com.tfg.aegis.group.mapper.GroupMapper;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;
import com.tfg.aegis.invitation.mapper.InvitationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
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
    private final GroupMapper groupMapper;
    private final CryptoService cryptoService;
    private final SecureRandom random = new SecureRandom();

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private static final Logger log = LoggerFactory.getLogger(InvitationService.class);


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

        LocalDateTime now = LocalDateTime.now();

        // 1) Reusar invitaciones activas
        List<Invitation> actives = invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(group, now);
        if (!actives.isEmpty()) {
            Invitation existing = actives.get(0);
            String code = decryptIfPresent(existing);

            log.info("Existing valid invitation found for group {}: {}", groupId, existing);
            return mapper.toDto(existing, code);
        }

        // 2) Crear una nueva
        String code = generateCode();
        String codeHash = passwordEncoder.encode(code);

        long minutes = (expiry == null) ? 60L : Math.max(1L, expiry); // evita 0 o negativos

        LocalDateTime expiresAt = now.plusMinutes(minutes);

        // Cifrado del código
        byte[] iv = new byte[12];
        random.nextBytes(iv);
        byte[] ciphertext;
        try {
            ciphertext = cryptoService.encrypt(code.getBytes(StandardCharsets.UTF_8), iv);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar el código de invitación", e);
        }

        Invitation invitation = new Invitation();
        invitation.setGroup(group);
        invitation.setExpiresAt(expiresAt);
        invitation.setCodeIv(iv);
        invitation.setCodeCiphertext(ciphertext);
        invitation.setCreatedAt(now);
        invitation.setCodeHash(codeHash);

        Invitation saved = invitationRepository.save(invitation);

        return mapper.toDto(saved, code);
    }

    private String decryptIfPresent(Invitation inv) {
        byte[] ct = inv.getCodeCiphertext();
        byte[] iv = inv.getCodeIv();
        if (ct == null || iv == null) return null; // filas legacy o sin cifrado

        try {
            byte[] plain = cryptoService.decrypt(ct, iv);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("No se pudo descifrar el código de Invitation id={}", inv.getId(), e);
            return null; // evita romper el flujo; devolverás null y el front ocultará el código
        }
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
    public GroupDto validateInvitation(String code) {
        if (code == null || code.isBlank()) return null;

        String input = code.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        List<Invitation> activeInvites = invitationRepository.findAllActive(now);
        for (Invitation inv : activeInvites) {
            if (passwordEncoder.matches(input, inv.getCodeHash())) {
                GroupDto dto = groupMapper.toDto(inv.getGroup());
                log.info("Invitation valid for group: {}", dto);
                return dto;
            }
        }
        log.info("Invalid invitation code: {}", input);

        return null;
    }
}
