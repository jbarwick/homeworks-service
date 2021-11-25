package com.jvj28.homeworks.data.model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "keypads")
public class Keypad implements Serializable {

    @Serial
    private static final long serialVersionUID = -5996194456256045358L;

    @Id
    @CsvBindByName
    private Integer id;

    @Column
    @CsvBindByName
    private String name;

    @Column
    @Indexed
    @CsvBindByName
    private String address;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Keypad keypads = (Keypad) o;
        return id != null && Objects.equals(id, keypads.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void copy(Keypad keypad) {
        this.id = keypad.id;
        this.name = keypad.name;
        this.address = keypad.address;
    }
}
