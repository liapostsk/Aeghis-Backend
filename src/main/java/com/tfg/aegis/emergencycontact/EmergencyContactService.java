package com.tfg.aegis.emergencycontact;

import com.tfg.aegis.emergencycontact.model.EmergencyContactDto;

public interface EmergencyContactService {

    /**
     * Adds a new emergency contact for the current user.
     *
     * @param emergencyContactDto the emergency contact data transfer object containing details of the contact
     */
    void addEmergencyContactForCurrentUser(EmergencyContactDto emergencyContactDto);

    /**
     * Edits an existing emergency contact for the current user.
     *
     * @param id                   the ID of the emergency contact to edit
     * @param emergencyContactDto  the updated emergency contact data transfer object
     */
    void editEmergencyContact(Long id, EmergencyContactDto emergencyContactDto);

    /**
     * Deletes an emergency contact for the current user.
     *
     * @param id the ID of the emergency contact to delete
     */
    void deleteEmergencyContactForCurrentUser(Long id);
}
