package com.jvj28.homeworks.model.db.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.opencsv.bean.CsvBindByName;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Table(name = "circuit_zones")
@Entity(name = "CircuitEntity")
public class CircuitEntity implements Serializable {

    private static final long serialVersionUID = -4838814670155025772L;

    @CsvBindByName
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @CsvBindByName
    @Column(name = "name")
    private String name;

    @CsvBindByName
    @Column(name = "address", nullable = false)
    private String address;

    @CsvBindByName
    @Column(name = "room")
    private String room;

    @CsvBindByName
    @Column(name = "lights")
    private int lights;

    @CsvBindByName
    @Column(name = "watts")
    private int watts;

    @CsvBindByName
    @Column(name = "type")
    private String type;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int level;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int rank;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWatts() {
        return watts;
    }

    public void setWatts(Integer watts) {
        this.watts = watts;
    }

    public Integer getLights() {
        return lights;
    }

    public void setLights(Integer lights) {
        this.lights = lights;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

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
        return "CircuitEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", room='" + room + '\'' +
                ", lights=" + lights +
                ", watts=" + watts +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", rank=" + rank +
                '}';
    }
}
