package com.jvj28.homeworks.data.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Immutable
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "watts_by_hour")
public class UsageByHour implements Serializable {

    private static final long serialVersionUID = -6747807612111577660L;

    @Id
    private java.util.Date date;

    @Column
    private int watts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UsageByHour that = (UsageByHour) o;
        return date != null && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
