package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.db.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersEntityRepository extends JpaRepository<UsersEntity, UUID> {

    Optional<UsersEntity> findByUserName(String username);

    @Query("select c.uid from UsersEntity c")
    List<UUID> findAllIds();

    @Query("select c.userName from UsersEntity c")
    List<String> findAllUsernames();

    @Query("delete from UsersEntity c where c.userName in (:names)")
    void deleteAllByUsernames(Collection<String> names);
}