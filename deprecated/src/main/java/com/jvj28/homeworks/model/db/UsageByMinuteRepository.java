package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.UsageByMinuteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface UsageByMinuteRepository extends CrudRepository<UsageByMinuteEntity, Long> {

    @Query("select c from UsageByMinuteEntity c where c.date >= :start_date and c.date < :end_date order by c.date")
    List<UsageByMinuteEntity> findUsageBetweenDate(@Param("start_date") Date start, @Param("end_date") Date end);

}
