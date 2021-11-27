package com.jvj28.homeworks.data.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Immutable
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "watts_by_day")
public class UsageByDayEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2553304008926973911L;

    @Id
    private java.util.Date date;

    @Column
    private String day;

    @Column
    private int watts;

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
