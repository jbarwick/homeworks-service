package com.jvj28.homeworks.data.db;

import com.jvj28.homeworks.data.model.UsageByHour;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UsageByHourRepository extends CrudRepository<UsageByHour, String> {

    @Query("select c from UsageByHour c where c.date >= :start_date and c.date < :end_date order by c.date")
    List<UsageByHour> findUsageBetweenDate(@Param("start_date") Date start, @Param("end_date") Date end);
}