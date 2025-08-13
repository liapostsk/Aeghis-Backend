package com.tfg.aegis.invitation;

import com.tfg.aegis.invitation.model.Invitation;
import com.tfg.aegis.invitation.model.InvitationDto;

import java.util.List;

public interface InvitationService {
    /**
     * Method that creates an Invitation
     * @param InvitationDto InvitationDto
     * @return Invitation id
     */
    InvitationDto createInvitation(Long groupId, Long expiry); // opcional: 30m ó 1d

    // Metodo que valida si una invitación es válida
    /**
     * Method that validates an invitation
     * @return true if the invitation is valid, false otherwise
     */
    Boolean validateInvitation(Long groupId, String code);

}