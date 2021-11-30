package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.db.entity.CircuitRankEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CircuitRankRepository extends CrudRepository<CircuitRankEntity, Integer> {

    @Query("select c from CircuitRankEntity c where c.uid = :userId order by c.rank")
    Iterable<CircuitRankEntity> findAllByUserId(UUID userId);

    @Query("select c.id from CircuitRankEntity c where c.uid = :uid order by c.rank")
    List<Integer> findAllKeysByUserId(UUID uid);

    @Query("delete from CircuitRankEntity c where c.id in (:ids)")
    void deleteAllWithIds(Collection<Integer> ids);

    @Query("delete from CircuitRankEntity c where c.uid = :uid")
    void deleteAllForUserId(UUID uid);
}
