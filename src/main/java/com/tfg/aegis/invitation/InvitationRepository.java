package com.tfg.aegis.invitation;

import com.tfg.aegis.invitation.model.Invitation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationRepository extends CrudRepository<Invitation, Long> {
    /**
     * Method to find active invitations by group ID.
     *
     * @param groupId the ID of the group
     * @return a list of active invitations for the specified group
     */
    @Query("""
       SELECT i FROM Invitation i
       WHERE i.group.id = :groupId
         AND i.revokedAt IS NULL
         AND i.expiresAt > :now
       ORDER BY i.expiresAt DESC
    """)
    List<Invitation> findActiveByGroupId(Long groupId, LocalDateTime now);
}
