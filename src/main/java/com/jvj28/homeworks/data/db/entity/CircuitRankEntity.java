package com.jvj28.homeworks.data.db.entity;

import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "CircuitRankEntity")
@Table(name = "circuit_rank")
public class CircuitRankEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2210877801950859110L;

    @Id
    private Integer id;

    @Column
    @org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID uid;

    @Column
    private int circuit_id;

    @Column
    private int rank;

    @Column
    private String address;

    private String name;

    private String room;

    private Integer lights;

    private Integer watts;

    private String type;

    @Column(name = "type")
    public String getType() {
        return type;
    }

    @Column(name = "watts")
    public Integer getWatts() {
        return watts;
    }

    @Column(name = "lights")
    public Integer getLights() {
        return lights;
    }

    @Column(name = "room")
    public String getRoom() {
        return room;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public int getCircuit_id() {
        return circuit_id;
    }

    public void setCircuit_id(int circuit_id) {
        this.circuit_id = circuit_id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setLights(Integer lights) {
        this.lights = lights;
    }

    public void setWatts(Integer watts) {
        this.watts = watts;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CircuitRankEntity that = (CircuitRankEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
