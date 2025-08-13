package com.tfg.aegis.safelocation;

import com.tfg.aegis.safelocation.model.SafeLocationDto;

public interface SafeLocationService {

    /**
     * Adds a new safe location for the current user.
     *
     * @param dto the safe location data transfer object containing details of the safe location
     */
    Long addSafeLocationForCurrentUser(SafeLocationDto dto);

    /**
     * Edits an existing safe location for the current user.
     *
     * @param id  the ID of the safe location to edit
     * @param dto the updated safe location data transfer object
     */
    void editSafeLocationForCurrentUser(Long id, SafeLocationDto dto);

    /**
     * Deletes a safe location for the current user.
     *
     * @param id the ID of the safe location to delete
     */
    void deleteSafeLocationForCurrentUser(Long id);

}
