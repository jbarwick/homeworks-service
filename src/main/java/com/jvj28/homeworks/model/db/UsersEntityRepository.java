package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface UsersEntityRepository extends CrudRepository<UsersEntity, UUID> {

    UsersEntity findByUsername(String username);

}