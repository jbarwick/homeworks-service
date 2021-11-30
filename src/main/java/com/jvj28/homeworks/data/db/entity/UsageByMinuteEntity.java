package com.jvj28.homeworks.data.db.entity;

import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Entity(name = "UsageByMinuteEntity")
@Table(name = "usage_by_minute")
public class UsageByMinuteEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2463817720193015011L;

    @Id
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name="date", nullable = false)
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

    public UsageByMinuteEntity() {
        this(Instant.now());
    }

    public UsageByMinuteEntity(Instant instant) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getYear_dow() {
        return year_dow;
    }

    public void setYear_dow(String year_dow) {
        this.year_dow = year_dow;
    }

    public String getYear_month() {
        return year_month;
    }

    public void setYear_month(String year_month) {
        this.year_month = year_month;
    }

    public String getYear_dow_hour() {
        return year_dow_hour;
    }

    public void setYear_dow_hour(String year_dow_hour) {
        this.year_dow_hour = year_dow_hour;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDow() {
        return dow;
    }

    public void setDow(int dow) {
        this.dow = dow;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWatts() {
        return watts;
    }

    public void setWatts(int watts) {
        this.watts = watts;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getYear_month_day() {
        return year_month_day;
    }

    public void setYear_month_day(String year_month_day) {
        this.year_month_day = year_month_day;
    }

    public String getYear_week() {
        return year_week;
    }

    public void setYear_week(String year_week) {
        this.year_week = year_week;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsageByMinuteEntity that = (UsageByMinuteEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
