package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CircuitZoneRepository extends CrudRepository<CircuitEntity, Integer> {

    //Query("select c from Circuit c where c.address = :address")
    Optional<CircuitEntity> findByAddress(String address);

    @Query("select c.id from CircuitEntity c")
    List<Integer> findAllKeys();

    @Query("select c.address from CircuitEntity c")
    List<String> findAllAddresses();

    @Modifying
    @Query("delete from CircuitEntity c where c.address = :address")
    void deleteWithAddress(String address);

    @Modifying
    @Query("delete from CircuitEntity c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
