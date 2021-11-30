package com.jvj28.homeworks.data.db.entity;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Immutable
@Entity(name = "UsageByDayEntity")
@Table(name = "watts_by_day")
public class UsageByDayEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2553304008926973911L;

    @Id
    @Column(name="date", nullable = false)
    private java.util.Date date;

    @Column(name="day", nullable = false)
    private String day;

    @Column(name="watts")
    private int watts;

    public Date getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public int getWatts() {
        return watts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsageByDayEntity that = (UsageByDayEntity) o;
        return date != null && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
