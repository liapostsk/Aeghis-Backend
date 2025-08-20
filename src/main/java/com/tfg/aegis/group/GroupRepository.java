package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    public List<Group> findAllByType(Enums.TypeGroup type);
}
