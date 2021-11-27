package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.KeypadData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeypadsRepository extends CrudRepository<KeypadData, Integer> {

    Optional<KeypadData> findByAddress(String address);

    @Query("select c.id from KeypadData c")
    List<Integer> findAllIds();

    @Query("select c.address from KeypadData c")
    List<String> findAllAddresses();

    @Query("delete from KeypadData c where c.address in (:addresses)")
    void deleteAllWithAddresses(Collection<String> addresses);
}
