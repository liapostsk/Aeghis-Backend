package com.tfg.aegis.repository;

import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.entity.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    List<Group> findByTypeAndMembers_Id(GroupEnums.TypeGroup type, Long userId);

    List<Group> findByMembers_Id(Long id);

    List<Group> findByOwnerId(Long ownerId);

    @Query("""
        SELECT g
        FROM Group g JOIN g.members m
        WHERE g.type = :type
          AND m.id = :userId
          AND (
              :type <> com.tfg.aegis.model.enums.GroupEnums.TypeGroup.TEMPORAL
              OR g.expirationDate IS NULL
              OR g.expirationDate > :now
          )
    """)
    List<Group> findByTypeAndMemberNotExpired(
            @Param("type") GroupEnums.TypeGroup type,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

}
