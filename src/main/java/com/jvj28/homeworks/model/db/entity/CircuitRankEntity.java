package com.jvj28.homeworks.model.db.entity;

import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "CircuitRankEntity")
@Table(name = "circuit_rank")
public class CircuitRankEntity implements Serializable {

    private static final long serialVersionUID = -2210877801950859110L;

    @Id
    @Column(name="id", nullable = false)
    private Integer id;

    @SuppressWarnings("deprecation")
    @Column(name="uid", nullable = false)
    @org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID uid;

    @Column(name="circuit_id", nullable = false)
    private int circuitId;

    @Column(name="rank")
    private int rank;

    @Column(name="address")
    private String address;

    @Transient
    private String name;

    @Transient
    private String room;

    @Transient
    private Integer lights;

    @Transient
    private Integer watts;

    @Transient
    private String type;

    public String getType() {
        return type;
    }

    public Integer getWatts() {
        return watts;
    }

    public Integer getLights() {
        return lights;
    }

    public String getRoom() {
        return room;
    }

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

    public int getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(int circuitId) {
        this.circuitId = circuitId;
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

    @Override
    public String toString() {
        return "CircuitRankEntity{" +
                "id=" + id +
                ", uid=" + uid +
                ", circuitId=" + circuitId +
                ", rank=" + rank +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", lights=" + lights +
                ", watts=" + watts +
                ", type='" + type + '\'' +
                '}';
    }
}
