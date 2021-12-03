package com.jvj28.homeworks.model.db.entity;

import com.opencsv.bean.CsvBindByName;
import org.springframework.data.redis.core.index.Indexed;

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
    @org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
    @CsvBindByName
    private UUID uid;

    @CsvBindByName
    @Column(name = "username", nullable = false)
    @Indexed
    private String userName;

    @CsvBindByName
    @Column(name = "userpass")
    private String userPass;

    @CsvBindByName
    @Column(name = "firstname", length = 50)
    private String firstName;

    @CsvBindByName
    @Column(name = "lastname", length = 50)
    private String lastName;

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userpass) {
        this.userPass = userpass;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public UUID getId() {
        return uid;
    }

    public void setId(UUID uid) {
        this.uid = uid;
    }
}