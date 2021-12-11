package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.CircuitRankEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CircuitRankRepository extends CrudRepository<CircuitRankEntity, Integer> {

    @Query("select c from CircuitRankEntity c where c.uid = :userId order by c.rank")
    Iterable<CircuitRankEntity> findAllByUserId(UUID userId);

    @Query("select c.id from CircuitRankEntity c where c.uid = :uid order by c.rank")
    List<Integer> findAllKeysByUserId(UUID uid);

    @Modifying
    @Query("delete from CircuitRankEntity c where c.id in (:ids)")
    void deleteAllWithIds(Collection<Integer> ids);

    @Modifying
    @Query("delete from CircuitRankEntity c where c.uid = :uid")
    void deleteAllForUserId(UUID uid);
}
