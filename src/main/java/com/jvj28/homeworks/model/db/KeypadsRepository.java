package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.KeypadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeypadsRepository extends CrudRepository<KeypadEntity, Integer> {

    Optional<KeypadEntity> findByAddress(String address);

    @Query("select c.id from KeypadEntity c")
    List<Integer> findAllIds();

    @Query("select c.address from KeypadEntity c")
    List<String> findAllAddresses();

    @Query("delete from KeypadEntity c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
