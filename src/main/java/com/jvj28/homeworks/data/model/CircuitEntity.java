package com.jvj28.homeworks.data.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opencsv.bean.CsvBindByName;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "circuit_zones")
public class CircuitEntity implements Serializable {

    private static final long serialVersionUID = -4838814670155025772L;

    @CsvBindByName
    @Id
    private Integer id;

    @CsvBindByName
    @Column
    private String name;

    @CsvBindByName
    @Column
    private String address;

    @CsvBindByName
    @Column
    private String room;

    @CsvBindByName
    @Column
    private int lights;

    @CsvBindByName
    @Column
    private int watts;

    @CsvBindByName
    @Column
    private String type;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int level;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int rank;

    @Transient
    private String error;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CircuitEntity that = (CircuitEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
