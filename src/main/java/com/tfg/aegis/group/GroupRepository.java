package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    List<Group> findByTypeAndMembers_Id(Enums.TypeGroup type, Long userId);

    List<Group> findByMembers_Id(Long id);
}
