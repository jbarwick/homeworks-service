package com.jvj28.homeworks.model.db.entity;

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

@Entity(name = "UsageByMinuteEntity")
@Table(name = "usage_by_minute")
public class UsageByMinuteEntity implements Serializable {

    private static final long serialVersionUID = -2463817720193015011L;

    @Id
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name="date", nullable = false)
    private Date date;

    @Column(name="year_dow")
    private String yearDow;

    @Column(name="year_month")
    private String yearMonth;

    @Column(name="year_dow_hour")
    private String yearDowHour;

    @Column(name="hour")
    private int hour;

    @Column(name="dow")
    private int dow;

    @Column(name="year")
    private int year;

    @Column(name="month")
    private int month;

    @Column(name="watts")
    private int watts;

    @Column(name="day")
    private int day;

    @Column(name="year_month_day")
    private String yearMonthDay;

    @Column(name="year_week")
    private String yearWeek;

    @Column(name="week")
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
        yearDow = String.format("%04d:%d", year, dow);
        yearMonth = String.format("%04d:%02d", year, month);
        yearDowHour = String.format("%04d:%d:%02d", year, dow, hour);
        yearMonthDay = String.format("%04d-%02d-%02d", year, month, day);
        yearWeek = String.format("%04d:%02d", year, week);
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

    public String getYearDow() {
        return yearDow;
    }

    public void setYearDow(String yearDow) {
        this.yearDow = yearDow;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getYearDowHour() {
        return yearDowHour;
    }

    public void setYearDowHour(String yearDowHour) {
        this.yearDowHour = yearDowHour;
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

    public String getYearMonthDay() {
        return yearMonthDay;
    }

    public void setYearMonthDay(String yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public String getYearWeek() {
        return yearWeek;
    }

    public void setYearWeek(String yearWeek) {
        this.yearWeek = yearWeek;
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

    @Override
    public String toString() {
        return "UsageByMinuteEntity{" +
                "id=" + id +
                ", date=" + date +
                ", yearDow='" + yearDow + '\'' +
                ", yearMonth='" + yearMonth + '\'' +
                ", yearDowHour='" + yearDowHour + '\'' +
                ", hour=" + hour +
                ", dow=" + dow +
                ", year=" + year +
                ", month=" + month +
                ", watts=" + watts +
                ", day=" + day +
                ", yearMonthDay='" + yearMonthDay + '\'' +
                ", yearWeek='" + yearWeek + '\'' +
                ", week=" + week +
                '}';
    }
}
