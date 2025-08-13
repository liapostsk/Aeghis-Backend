package com.tfg.aegis.group;

import com.tfg.aegis.group.model.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {
    // Busca grupos donde exista un usuario con ese id en la colección users

}
