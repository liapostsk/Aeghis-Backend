package com.tfg.aegis.repository;

import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.entity.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    List<Group> findByTypeAndMembers_Id(GroupEnums.TypeGroup type, Long userId);

    List<Group> findByMembers_Id(Long id);
}
