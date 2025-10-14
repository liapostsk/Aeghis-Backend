package com.tfg.aegis.invitation;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.invitation.model.Invitation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationRepository extends CrudRepository<Invitation, Long> {
    @Query("SELECT i FROM Invitation i WHERE i.expiresAt > :now")
    List<Invitation> findAllActive(@Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from Invitation i where i.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    List<Invitation> findByGroupAndExpiresAtAfterAndRevokedAtIsNullOrderByCreatedAtDesc(
            Group group, LocalDateTime now
    );
}
