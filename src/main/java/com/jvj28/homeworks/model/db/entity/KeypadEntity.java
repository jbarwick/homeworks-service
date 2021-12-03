package com.jvj28.homeworks.model.db.entity;

import com.opencsv.bean.CsvBindByName;
import org.hibernate.Hibernate;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "KeypadEntity")
@Table(name = "keypads")
public class KeypadEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -5996194456256045358L;

    @Id
    @CsvBindByName
    @Column(name="id", nullable = false)
    private Integer id;

    @Column
    @CsvBindByName
    private String name;

    @Column(name="address", nullable = false)
    @Indexed
    @CsvBindByName
    private String address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        KeypadEntity keypads = (KeypadEntity) o;
        return id != null && Objects.equals(id, keypads.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void copy(KeypadEntity keypad) {
        this.id = keypad.id;
        this.name = keypad.name;
        this.address = keypad.address;
    }
}
