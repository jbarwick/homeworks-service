package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.UsageByDayEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UsageByDayRepository extends CrudRepository<UsageByDayEntity, String> {

    @Query("select c from UsageByDayEntity c where c.date >= :start_date and c.date < :end_date order by c.date")
    List<UsageByDayEntity> findUsageBetweenDate(@Param("start_date") Date start, @Param("end_date") Date end);
}
