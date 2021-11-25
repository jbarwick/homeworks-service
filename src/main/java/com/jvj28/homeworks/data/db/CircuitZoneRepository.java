package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.Circuit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CircuitZoneRepository extends CrudRepository<Circuit, Integer> {

    //Query("select c from Circuit c where c.address = :address")
    Optional<Circuit> findByAddress(String address);

    @Query("select id from Circuit")
    List<Integer> findAllKeys();

    @Query("select address from Circuit")
    List<String> findAllAddresses();

    @Query("delete from Circuit c where c.address = :address")
    void deleteWithAddress(String address);

    @Query("delete from Circuit c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
