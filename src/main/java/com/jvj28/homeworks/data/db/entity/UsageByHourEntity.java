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
@Entity(name = "UsageByHourEntity")
@Table(name = "watts_by_hour")
public class UsageByHourEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6747807612111577660L;

    @Id
    private java.util.Date date;

    @Column
    private int watts;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getWatts() {
        return watts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsageByHourEntity that = (UsageByHourEntity) o;
        return date != null && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
