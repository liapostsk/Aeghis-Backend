package com.tfg.aegis.emergencycontact;

import com.tfg.aegis.common.exception.ApiException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.emergencycontact.mapper.EmergencyContactMapper;
import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;
import com.tfg.aegis.emergencycontact.model.Enums;
import com.tfg.aegis.externalcontact.ExternalContactRepository;
import com.tfg.aegis.externalcontact.model.ExternalContact;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.UserService;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    @Mock private EmergencyContactRepository emergencyContactRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private ExternalContactRepository externalContactRepository;
    @Mock private EmergencyContactMapper emergencyContactMapper;

    @InjectMocks
    private EmergencyContactService service;

    private static final String CLERK_ID = "clerk_abc";

    @BeforeEach
    void setUpSecurity() {
        Authentication auth = new UsernamePasswordAuthenticationToken(CLERK_ID, "n/a");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /* =========================
     * addEmergencyContactForCurrentUser
     * ========================= */
    @Test
    void addEmergencyContact_ok_mapsSavesAndReturnsDto() {
        // owner actual
        User owner = new User(); owner.setId(1L); owner.setClerkId(CLERK_ID); owner.setPhone("+34111111111");
        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.of(owner));

        // contact (usuario de la app)
        User contact = new User(); contact.setId(22L); contact.setPhone("+34999999999");
        EmergencyContactDto input = new EmergencyContactDto();
        input.setContactId(22L);
        when(userRepository.findById(22L)).thenReturn(Optional.of(contact));

        // mapper entity <-> dto
        EmergencyContact entity = new EmergencyContact();
        EmergencyContactDto expectedDto = new EmergencyContactDto(); expectedDto.setContactId(22L);

        when(emergencyContactMapper.toEntity(input)).thenReturn(entity);
        when(emergencyContactRepository.save(entity)).thenAnswer(ans -> {
            entity.setId(77L);
            return entity;
        });
        when(emergencyContactMapper.toDto(entity)).thenReturn(expectedDto);

        EmergencyContactDto out = service.addEmergencyContactForCurrentUser(input);

        assertSame(expectedDto, out);
        assertEquals(owner, entity.getOwner());
        assertEquals(contact, entity.getContact());
        verify(emergencyContactRepository).save(entity);
    }

    @Test
    void addEmergencyContact_ownerNotFound_throwsNotFound() {
        EmergencyContactDto input = new EmergencyContactDto(); input.setContactId(2L);
        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addEmergencyContactForCurrentUser(input));
        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void addEmergencyContact_contactNotInApp_throwsApiExceptionNotFound() {
        User owner = new User(); owner.setId(1L); owner.setClerkId(CLERK_ID); owner.setPhone("+34111111111");
        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.of(owner));

        EmergencyContactDto input = new EmergencyContactDto(); input.setContactId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.addEmergencyContactForCurrentUser(input));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("USER_NOT_IN_APP", ex.getTitle());
        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void addEmergencyContact_selfAdd_throwsApiExceptionBadRequest() {
        User owner = new User(); owner.setId(1L); owner.setClerkId(CLERK_ID); owner.setPhone("+34111111111");
        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.of(owner));

        User contact = new User(); contact.setId(1L); contact.setPhone("+34111111111"); // mismo teléfono → self
        EmergencyContactDto input = new EmergencyContactDto(); input.setContactId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(contact));

        ApiException ex = assertThrows(ApiException.class, () -> service.addEmergencyContactForCurrentUser(input));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(emergencyContactRepository, never()).save(any());
    }

    /* =========================
     * editEmergencyContact
     * ========================= */
    @Test
    void editEmergencyContact_ok_updatesRelation_whenOwned() {
        // usuario actual
        UserDto me = new UserDto(); me.setId(10L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        // contacto existente y owned
        User owner = new User(); owner.setId(10L);
        EmergencyContact contact = new EmergencyContact();
        contact.setId(5L);
        contact.setOwner(owner);
        contact.setRelation("OLD");

        when(emergencyContactRepository.findById(5L)).thenReturn(Optional.of(contact));

        EmergencyContactDto incoming = new EmergencyContactDto();
        incoming.setRelation("NEW");

        service.editEmergencyContact(5L, incoming);

        assertEquals("NEW", contact.getRelation());
        verify(emergencyContactRepository).save(contact);
    }

    @Test
    void editEmergencyContact_notOwned_throwsAccessDenied() {
        UserDto me = new UserDto(); me.setId(1L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User otherOwner = new User(); otherOwner.setId(2L);
        EmergencyContact contact = new EmergencyContact(); contact.setId(5L); contact.setOwner(otherOwner);

        when(emergencyContactRepository.findById(5L)).thenReturn(Optional.of(contact));

        assertThrows(AccessDeniedException.class, () -> service.editEmergencyContact(5L, new EmergencyContactDto()));
        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void editEmergencyContact_notFound_throwsNotFound() {
        UserDto me = new UserDto(); me.setId(1L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);
        when(emergencyContactRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.editEmergencyContact(404L, new EmergencyContactDto()));
    }

    /* =========================
     * deleteEmergencyContactForCurrentUser
     * ========================= */
    @Test
    void deleteEmergencyContact_ok_whenRecordExistedAndNowDeleted() {
        Long id = 7L;
        when(emergencyContactRepository.existsById(id)).thenReturn(true, false); // existed=true, after delete=false

        service.deleteEmergencyContactForCurrentUser(id);

        verify(emergencyContactRepository).deleteById(id);
    }

    @Test
    void deleteEmergencyContact_failedDeletion_throwsIllegalState() {
        Long id = 8L;
        when(emergencyContactRepository.existsById(id)).thenReturn(true, true); // existed=true, still true after delete

        assertThrows(IllegalStateException.class, () -> service.deleteEmergencyContactForCurrentUser(id));
        verify(emergencyContactRepository).deleteById(id);
    }

    @Test
    void deleteEmergencyContact_whenNotExistedBefore_noException() {
        Long id = 9L;
        when(emergencyContactRepository.existsById(id)).thenReturn(false); // existed=false

        assertDoesNotThrow(() -> service.deleteEmergencyContactForCurrentUser(id));
        verify(emergencyContactRepository).deleteById(id);
    }

    /* =========================
     * requestPromoteExternalToEmergencyContact
     * ========================= */
    @Test
    void requestPromoteExternalToEmergencyContact_ok_createsPendingAndDeletesExternal() {
        Long userId = 11L;

        User owner = new User(); owner.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        ExternalContact ext = new ExternalContact();
        ext.setRelation("family");
        ext.setPhone("+34987654321");

        User contactUser = new User(); contactUser.setId(33L); contactUser.setPhone("+34987654321");
        when(userRepository.findByPhone("+34987654321")).thenReturn(Optional.of(contactUser));

        ArgumentCaptor<EmergencyContact> ecCaptor = ArgumentCaptor.forClass(EmergencyContact.class);
        when(emergencyContactRepository.save(ecCaptor.capture())).thenAnswer(ans -> {
            EmergencyContact ec = ecCaptor.getValue();
            ec.setId(555L);
            return ec;
        });

        Long id = service.requestPromoteExternalToEmergencyContact(ext, userId);

        assertEquals(555L, id);
        EmergencyContact saved = ecCaptor.getValue();
        assertEquals(owner, saved.getOwner());
        assertEquals(contactUser, saved.getContact());
        assertEquals("family", saved.getRelation());
        assertEquals(Enums.Status.PENDING, saved.getStatus());
        verify(externalContactRepository).delete(ext);
    }

    @Test
    void requestPromoteExternalToEmergencyContact_ownerNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.requestPromoteExternalToEmergencyContact(new ExternalContact(), 1L));
        verify(emergencyContactRepository, never()).save(any());
    }

    @Test
    void requestPromoteExternalToEmergencyContact_contactByPhoneNotFound_throwsNotFound() {
        Long userId = 1L;
        User owner = new User(); owner.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        ExternalContact ext = new ExternalContact();
        ext.setPhone("+34000000000");

        when(userRepository.findByPhone("+34000000000")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.requestPromoteExternalToEmergencyContact(ext, userId));
        verify(emergencyContactRepository, never()).save(any());
        verify(externalContactRepository, never()).delete(any());
    }

    /* =========================
     * acceptEmergencyContact
     * ========================= */
    @Test
    void acceptEmergencyContact_whenPending_setsAcceptedAndSaves() {
        EmergencyContact ec = new EmergencyContact();
        ec.setId(77L);
        ec.setStatus(Enums.Status.PENDING);

        when(emergencyContactRepository.findById(77L)).thenReturn(Optional.of(ec));

        service.acceptEmergencyContact(77L);

        assertEquals(Enums.Status.ACCEPTED, ec.getStatus());
        verify(emergencyContactRepository).save(ec);
    }

    @Test
    void acceptEmergencyContact_whenAlreadyAccepted_doesNothing() {
        EmergencyContact ec = new EmergencyContact();
        ec.setId(88L);
        ec.setStatus(Enums.Status.ACCEPTED);

        when(emergencyContactRepository.findById(88L)).thenReturn(Optional.of(ec));

        service.acceptEmergencyContact(88L);
    }

    @Test
    void acceptEmergencyContact_notFound_throwsNotFound() {
        when(emergencyContactRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.acceptEmergencyContact(404L));
    }
}
