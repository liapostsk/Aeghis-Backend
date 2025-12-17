package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.repository.ExternalContactRepository;
import com.tfg.aegis.model.mapper.ExternalContactMapper;
import com.tfg.aegis.model.entity.ExternalContact;
import com.tfg.aegis.model.dto.ExternalContactDto;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalContactServiceTest {

    @Mock private ExternalContactRepository externalContactRepository;
    @Mock private UserRepository userRepository;
    @Mock private ExternalContactMapper externalContactMapper;
    @Mock private UserService userService;

    @InjectMocks
    private ExternalContactService service;

    private static final String CLERK_ID = "clerk_123";

    @BeforeEach
    void setUpSecurity() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(CLERK_ID);
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    /* =========================
     * createExternalContactForCurrentUser
     * ========================= */
    @Test
    void createExternalContact_ok_setsOwner_andSaves_andReturnsId() {
        ExternalContactDto dto = new ExternalContactDto();

        User owner = new User();
        owner.setId(10L);
        owner.setClerkId(CLERK_ID);
        owner.setName("Owner");
        owner.setPhone("+34111111111");
        owner.setEmail("owner@test.local");
        owner.setAcceptedPrivacyPolicy(true);
        owner.setExternalContacts(new HashSet<>()); // importante para add()

        ExternalContact entity = new ExternalContact();

        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.of(owner));
        when(externalContactMapper.toEntity(dto)).thenReturn(entity);
        when(externalContactRepository.save(entity)).thenAnswer(inv -> {
            entity.setId(999L);
            return entity;
        });

        Long outId = service.createExternalContactForCurrentUser(dto);

        assertEquals(999L, outId);
        assertSame(owner, entity.getOwner());
        assertTrue(owner.getExternalContacts().contains(entity));
        verify(externalContactRepository).save(entity);
    }

    @Test
    void createExternalContact_ownerNotFound_throwsNotFound() {
        ExternalContactDto dto = new ExternalContactDto();
        when(userRepository.findByClerkId(CLERK_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createExternalContactForCurrentUser(dto));
        verify(externalContactRepository, never()).save(any());
    }

    /* =========================
     * editExternalContact
     * ========================= */
    @Test
    void editExternalContact_ok_updatesNameTrim_andPhoneNormalized_andRelation() {
        Long id = 5L;

        UserDto me = new UserDto(); me.setId(10L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(10L);

        ExternalContact current = new ExternalContact();
        current.setId(id);
        current.setOwner(owner);
        current.setName("Old");
        current.setPhone("+34600111222"); // ya normalizado
        current.setRelation("friend");

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(current));

        ExternalContactDto incoming = new ExternalContactDto();
        incoming.setName("  New Name  ");              // trim
        incoming.setPhone(" +34 600 111 222 ");        // normalización (quita espacios)
        incoming.setRelation("family");

        service.editExternalContact(id, incoming);

        assertEquals("New Name", current.getName());
        assertEquals("+34600111222", current.getPhone()); // mismo número tras normalizar
        assertEquals("family", current.getRelation());
    }

    @Test
    void editExternalContact_ok_phoneChanged_checksDuplicates_andUpdates() {
        Long id = 6L;

        UserDto me = new UserDto(); me.setId(77L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(77L);

        ExternalContact current = new ExternalContact();
        current.setId(id);
        current.setOwner(owner);
        current.setName("Old");
        current.setPhone("+34600111222");
        current.setRelation("friend");

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(current));

        ExternalContactDto incoming = new ExternalContactDto();
        incoming.setName("Name");
        incoming.setPhone(" +34 600 333 444 ");  // → "+34600333444"
        incoming.setRelation("colleague");

        when(externalContactRepository.findFirstByOwnerIdAndPhone(77L, "+34600333444"))
                .thenReturn(Optional.empty());

        service.editExternalContact(id, incoming);

        assertEquals("+34600333444", current.getPhone());
        assertEquals("colleague", current.getRelation());
    }

    @Test
    void editExternalContact_phoneChanged_butDuplicateWithDifferentId_throwsDataIntegrity() {
        Long id = 7L;

        UserDto me = new UserDto(); me.setId(42L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(42L);

        ExternalContact current = new ExternalContact();
        current.setId(id);
        current.setOwner(owner);
        current.setPhone("+34600111222");

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(current));

        ExternalContactDto incoming = new ExternalContactDto();
        incoming.setName("X");
        incoming.setPhone(" +34 600 111 333 "); // → "+34600111333"

        ExternalContact existingOther = new ExternalContact();
        existingOther.setId(100L);
        existingOther.setOwner(owner);

        when(externalContactRepository.findFirstByOwnerIdAndPhone(42L, "+34600111333"))
                .thenReturn(Optional.of(existingOther));

        assertThrows(DataIntegrityViolationException.class, () -> service.editExternalContact(id, incoming));
        assertEquals("+34600111222", current.getPhone());
    }

    @Test
    void editExternalContact_phoneChanged_duplicateButSameEntity_allowsUpdate() {
        Long id = 8L;

        UserDto me = new UserDto(); me.setId(8L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(8L);

        ExternalContact current = new ExternalContact();
        current.setId(id);
        current.setOwner(owner);
        current.setPhone("+34600000000");

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(current));

        ExternalContactDto incoming = new ExternalContactDto();
        incoming.setName("Name");
        incoming.setPhone(" +34 600 000 000 "); // "+34600000000"

        service.editExternalContact(id, incoming);

        assertEquals("+34600000000", current.getPhone());
        assertEquals("Name", current.getName());
    }

    /* =========================
     * deleteExternalContact
     * ========================= */
    @Test
    void deleteExternalContact_ok_whenOwned() {
        Long id = 9L;

        UserDto me = new UserDto(); me.setId(77L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(77L);

        ExternalContact entity = new ExternalContact();
        entity.setId(id);
        entity.setOwner(owner);

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(entity));

        service.deleteExternalContact(id);

        verify(externalContactRepository).delete(entity);
    }

    @Test
    void deleteExternalContact_notOwned_throwsAccessDenied() {
        Long id = 10L;

        UserDto me = new UserDto(); me.setId(1L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        User owner = new User(); owner.setId(2L);

        ExternalContact entity = new ExternalContact();
        entity.setId(id);
        entity.setOwner(owner);

        when(externalContactRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(AccessDeniedException.class, () -> service.deleteExternalContact(id));
        verify(externalContactRepository, never()).delete(any());
    }

    /* =========================
     * getExternalContactOrThrow (indirecto) - NotFound
     * ========================= */
    @Test
    void getExternalContactOrThrow_notFound_throwsNotFound() {
        Long id = 404L;

        UserDto me = new UserDto(); me.setId(10L);
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(me);

        when(externalContactRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteExternalContact(id));
    }
}
