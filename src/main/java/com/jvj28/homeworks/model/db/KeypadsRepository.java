package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.KeypadEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KeypadsRepository extends CrudRepository<KeypadEntity, Integer> {

    Optional<KeypadEntity> findByAddress(String address);

    @Query("select c.id from KeypadEntity c")
    List<Integer> findAllIds();

    @Query("select c.address from KeypadEntity c")
    List<String> findAllAddresses();

    @Modifying
    @Query("delete from KeypadEntity c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
