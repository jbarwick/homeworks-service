package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.CircuitRank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CircuitRankRepository extends CrudRepository<CircuitRank, Integer> {

    @Query("select c from CircuitRank c where c.uid = :userId order by c.rank")
    Iterable<CircuitRank> findAllByUserId(UUID userId);

    @Query("select c.id from CircuitRank c where c.uid = :userId order by c.rank")
    List<Integer> findAllKeysByUserId(UUID userId);

    @Query("delete from CircuitRank c where c.id in (:ids)")
    void deleteAllWithIds(Collection<Integer> ids);
}
