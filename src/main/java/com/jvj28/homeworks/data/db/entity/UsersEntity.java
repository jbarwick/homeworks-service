package com.jvj28.homeworks.data.db.entity;

import com.opencsv.bean.CsvBindByName;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Table(name = "users", indexes = {
        @Index(name = "users_username_uindex", columnList = "username", unique = true)
})
@Entity(name = "UsersEntity")
public class UsersEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8014989191927735315L;

    @Id
    @Column(name = "uid", nullable = false)
    @CsvBindByName
    private UUID uid;

    @CsvBindByName
    @Column(name = "username", nullable = false)
    private String username;

    @CsvBindByName
    @Column(name = "userpass")
    private String userpass;

    @CsvBindByName
    @Column(name = "firstname", length = 50)
    private String firstname;

    @CsvBindByName
    @Column(name = "lastname", length = 50)
    private String lastname;

    @CsvBindByName
    @Lob
    @Column(name = "info")
    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getUserpass() {
        return userpass;
    }

    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getId() {
        return uid;
    }

    public void setId(UUID uid) {
        this.uid = uid;
    }
}