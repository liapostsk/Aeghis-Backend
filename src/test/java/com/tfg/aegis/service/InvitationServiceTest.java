package com.tfg.aegis.service;

import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.model.mapper.InvitationMapper;
import com.tfg.aegis.model.entity.Invitation;
import com.tfg.aegis.model.dto.InvitationDto;
import com.tfg.aegis.repository.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock private InvitationRepository invitationRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private InvitationMapper invitationMapper;
    @Mock private GroupMapper groupMapper;
    @Mock private CryptoService cryptoService;

    @InjectMocks
    private InvitationService service;

    /* =========================
     * createInvitation - reutiliza activa
     * ========================= */
    @Test
    void createInvitation_reusesActive_invitation_andDecryptsCode() throws Exception {
        Long groupId = 10L;

        Group group = new Group();
        group.setId(groupId);

        Invitation existing = new Invitation();
        existing.setId(1L);
        existing.setGroup(group);
        existing.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(55));
        existing.setCodeIv("iv-iv-iv".getBytes(StandardCharsets.UTF_8));
        existing.setCodeCiphertext("ct-ct".getBytes(StandardCharsets.UTF_8));

        InvitationDto dto = new InvitationDto(); // lo devolverá el mapper

        when(groupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(eq(group), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));
        when(cryptoService.decrypt(existing.getCodeCiphertext(), existing.getCodeIv()))
                .thenReturn("INV-CODE".getBytes(StandardCharsets.UTF_8));
        when(invitationMapper.toDto(existing, "INV-CODE")).thenReturn(dto);

        InvitationDto out = service.createInvitation(groupId, null);

        assertSame(dto, out);
        // No debe crear nueva invitación ni encriptar/hashear nuevo código
        verify(invitationRepository, never()).save(argThat(i -> i != existing));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createInvitation_reusesActive_butDecryptFails_returnsDtoWithNullCode() throws Exception {
        Long groupId = 10L;
        Group group = new Group(); group.setId(groupId);

        Invitation existing = new Invitation();
        existing.setId(1L);
        existing.setGroup(group);
        existing.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(55));
        existing.setCodeIv(new byte[]{1,2,3});
        existing.setCodeCiphertext(new byte[]{4,5,6});

        InvitationDto dto = new InvitationDto();

        when(groupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(eq(group), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));
        // Falla descifrado
        when(cryptoService.decrypt(any(), any())).thenThrow(new RuntimeException("boom"));
        // Mapper recibe code == null
        when(invitationMapper.toDto(existing, null)).thenReturn(dto);

        InvitationDto out = service.createInvitation(groupId, 60L);

        assertSame(dto, out);
        verify(invitationMapper).toDto(existing, null);
        verify(invitationRepository, never()).save(argThat(i -> i != existing));
    }

    /* =========================
     * createInvitation - crea nueva
     * ========================= */
    @Test
    void createInvitation_createsNew_withDefaultExpiry60_andEncryptsAndHashes() throws Exception {
        Long groupId = 20L;
        Group group = new Group(); group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(eq(group), any(LocalDateTime.class)))
                .thenReturn(List.of()); // no hay activas

        // Capturamos el código aleatorio que genera el servicio (desconocido a priori)
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        when(passwordEncoder.encode(codeCaptor.capture())).thenReturn("hash");

        // Simulamos encrypt: validamos que recibe el mismo código capturado
        when(cryptoService.encrypt(any(byte[].class), any(byte[].class))).thenAnswer(invocation -> {
            byte[] plain = invocation.getArgument(0, byte[].class);
            String plainStr = new String(plain, StandardCharsets.UTF_8);
            assertEquals(codeCaptor.getValue(), plainStr, "el texto cifrado debe ser el código generado");
            return "ciphertext".getBytes(StandardCharsets.UTF_8);
        });

        ArgumentCaptor<Invitation> invCaptor = ArgumentCaptor.forClass(Invitation.class);
        Invitation saved = new Invitation(); saved.setId(99L);
        when(invitationRepository.save(invCaptor.capture())).thenReturn(saved);

        InvitationDto dto = new InvitationDto();
        when(invitationMapper.toDto(eq(saved), anyString())).thenReturn(dto);

        LocalDateTime before = LocalDateTime.now();
        InvitationDto out = service.createInvitation(groupId, null);
        LocalDateTime after = LocalDateTime.now();

        assertSame(dto, out);

        Invitation toSave = invCaptor.getValue();
        assertSame(group, toSave.getGroup());
        assertNotNull(toSave.getCreatedAt());
        assertNotNull(toSave.getExpiresAt());
        assertNotNull(toSave.getCodeIv());
        assertNotNull(toSave.getCodeCiphertext());
        assertEquals("hash", toSave.getCodeHash());

        // Por defecto expiry = 60 min -> diferencia entre expiresAt y createdAt ≈ 60
        long minutes = Duration.between(toSave.getCreatedAt(), toSave.getExpiresAt()).toMinutes();
        assertEquals(60L, minutes, "expiry por defecto debe ser 60 minutos");

        // El mapper recibe el mismo 'code' generado internamente
        verify(invitationMapper).toDto(saved, codeCaptor.getValue());
    }

    @Test
    void createInvitation_createsNew_withExpiryFloorTo1Minute_whenZeroOrNegative() throws Exception {
        Long groupId = 30L;
        Group group = new Group(); group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(eq(group), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(cryptoService.encrypt(any(), any())).thenReturn("ct".getBytes(StandardCharsets.UTF_8));

        ArgumentCaptor<Invitation> invCaptor = ArgumentCaptor.forClass(Invitation.class);
        when(invitationRepository.save(invCaptor.capture())).thenAnswer(ans -> invCaptor.getValue());

        when(invitationMapper.toDto(any(Invitation.class), anyString())).thenReturn(new InvitationDto());

        service.createInvitation(groupId, 0L); // 0 -> debe aplicar Math.max(1, expiry)

        Invitation saved = invCaptor.getValue();
        long minutes = Duration.between(saved.getCreatedAt(), saved.getExpiresAt()).toMinutes();
        assertEquals(1L, minutes, "expiry debe ajustarse a 1 minuto como mínimo");
    }

    /* =========================
     * validateInvitation
     * ========================= */
    @Test
    void validateInvitation_nullOrBlank_returnsNull_andSkipsRepo() {
        assertNull(service.validateInvitation(null));
        assertNull(service.validateInvitation("   "));
        verify(invitationRepository, never()).findAllActive(any());
    }

    @Test
    void validateInvitation_validCode_returnsGroupDto() {
        String input = "inv-abc"; // se normaliza a upper
        Invitation inv = new Invitation();
        Group group = new Group(); group.setId(77L);
        inv.setGroup(group);
        inv.setCodeHash("hash123");

        when(invitationRepository.findAllActive(any(LocalDateTime.class))).thenReturn(List.of(inv));
        when(passwordEncoder.matches(eq("INV-ABC"), anyString())).thenReturn(true); // primer match
        GroupDto dto = new GroupDto(); dto.setId(77L);
        when(groupMapper.toDto(group)).thenReturn(dto);

        GroupDto out = service.validateInvitation(input);

        assertNotNull(out);
        assertEquals(77L, out.getId());
        verify(groupMapper).toDto(group);
    }

    @Test
    void validateInvitation_noMatch_returnsNull() {
        Invitation inv1 = new Invitation(); inv1.setCodeHash("h1");
        Invitation inv2 = new Invitation(); inv2.setCodeHash("h2");

        when(invitationRepository.findAllActive(any(LocalDateTime.class)))
                .thenReturn(List.of(inv1, inv2));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        GroupDto out = service.validateInvitation("SOMETHING");
        assertNull(out);
        verify(groupMapper, never()).toDto(any());
    }

    /* =========================
     * createInvitation - errores
     * ========================= */
    @Test
    void createInvitation_groupNotFound_throws() {
        when(groupRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.createInvitation(999L, null));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void createInvitation_encryptFails_throwsIllegalState() throws Exception {
        Long groupId = 50L;
        Group group = new Group(); group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(invitationRepository.findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(eq(group), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(cryptoService.encrypt(any(), any())).thenThrow(new RuntimeException("cipher err"));

        assertThrows(IllegalStateException.class, () -> service.createInvitation(groupId, 30L));
        verify(invitationRepository, never()).save(any());
    }
}
