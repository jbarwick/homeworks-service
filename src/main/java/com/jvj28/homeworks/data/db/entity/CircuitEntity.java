package com.jvj28.homeworks.data.db.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opencsv.bean.CsvBindByName;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Table(name = "circuit_zones")
@Entity(name = "CircuitEntity")
public class CircuitEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4838814670155025772L;

    @CsvBindByName
    @Id
    @Column(name = "id", nullable = false)
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

    @Lob
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "watts")
    public Integer getWatts() {
        return watts;
    }

    public void setWatts(Integer watts) {
        this.watts = watts;
    }

    @Column(name = "lights")
    public Integer getLights() {
        return lights;
    }

    public void setLights(Integer lights) {
        this.lights = lights;
    }

    @Lob
    @Column(name = "room")
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLights(int lights) {
        this.lights = lights;
    }

    public void setWatts(int watts) {
        this.watts = watts;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "address = " + address + ", " +
                "room = " + room + ", " +
                "lights = " + lights + ", " +
                "watts = " + watts + ", " +
                "type = " + type + ")";
    }
}
