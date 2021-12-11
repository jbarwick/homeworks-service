package com.jvj28.homeworks.model.db.entity;

import com.opencsv.bean.CsvBindByName;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Table(name = "users", indexes = {
        @Index(name = "users_username_uindex", columnList = "username", unique = true)
})
@Entity(name = "UsersEntity")
public class UsersEntity implements Serializable {

    private static final long serialVersionUID = -8014989191927735315L;

    @SuppressWarnings("deprecation")
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
    @Column(name = "info")
    private String info;

    public UsersEntity() {
        // new entity doesn't need to initialize values
    }

    public UUID getId() {
        return uid;
    }

    public void setId(UUID uid) {
        this.uid = uid;
    }


    public UsersEntity(String userName, String userPass) {
        this.userName = userName;
        this.userPass = userPass;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersEntity that = (UsersEntity) o;
        return uid.equals(that.uid) && userName.equals(that.userName) && Objects.equals(userPass, that.userPass) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, userName, userPass, firstName, lastName, info);
    }

    @Override
    public String toString() {
        return "UsersEntity{" +
                "uid=" + uid +
                ", userName='" + userName + '\'' +
                ", userPass='" + userPass + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}