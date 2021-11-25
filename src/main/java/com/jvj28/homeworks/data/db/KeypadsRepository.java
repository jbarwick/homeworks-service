package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.Keypad;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeypadsRepository extends CrudRepository<Keypad, Integer> {

    Optional<Keypad> findByAddress(String address);

    @Query("select c.id from Keypad c")
    List<Integer> findAllIds();

    @Query("select c.address from Keypad c")
    List<String> findAllAddresses();

    @Query("delete from Keypad c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
