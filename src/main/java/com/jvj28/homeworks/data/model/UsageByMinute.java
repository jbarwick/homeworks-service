package com.jvj28.homeworks.data.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "usage_by_minute")
public class UsageByMinute implements Serializable {

    private static final long serialVersionUID = -2463817720193015011L;

    @Id
    private Long id;

    @Column
    private Date date;

    @Column
    private String year_dow;

    @Column
    private String year_month;

    @Column
    private String year_dow_hour;

    @Column
    private int hour;

    @Column
    private int dow;

    @Column
    private int year;

    @Column
    private int month;

    @Column
    private int watts;

    @Column
    private int day;

    @Column
    private String year_month_day;

    @Column
    private String year_week;

    @Column
    private int week;

    public UsageByMinute(Instant instant) {
        ZonedDateTime dtm = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES);
        this.date = Date.from(dtm.toInstant());
        id =  dtm.toEpochSecond();
        day = dtm.getDayOfMonth();
        dow = dtm.getDayOfWeek().getValue();
        hour = dtm.getHour();
        year = dtm.getYear();
        month = dtm.getMonthValue();
        week = dtm.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        year_dow = String.format("%04d:%d", year, dow);
        year_month = String.format("%04d:%02d", year, month);
        year_dow_hour = String.format("%04d:%d:%02d", year, dow, hour);
        year_month_day = String.format("%04d-%02d-%02d", year, month, day);
        year_week = String.format("%04d:%02d", year, week);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsageByMinute that = (UsageByMinute) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
