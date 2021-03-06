package com.jvj28.homeworks.model.db;

import com.jvj28.homeworks.model.db.entity.UsageByHourEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface UsageByHourRepository extends CrudRepository<UsageByHourEntity, String> {

    @Query("select c from UsageByHourEntity c where c.date >= :start_date and c.date < :end_date order by c.date")
    List<UsageByHourEntity> findUsageBetweenDate(@Param("start_date") Date start, @Param("end_date") Date end);
}
