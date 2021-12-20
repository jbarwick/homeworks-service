package com.jvj28.homeworks.model.db.entity;

import com.opencsv.bean.CsvBindByName;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table(name = "users", indexes = {
        @Index(name = "users_username_uindex", columnList = "username", unique = true)
})
@Entity(name = "UsersEntity")
public class UsersEntity implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = -8014989191927735315L;

    @NotNull
    @SuppressWarnings("deprecation")
    @Id
    @Column(name = "uid", nullable = false)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @CsvBindByName
    private UUID uid;

    @NotNull
    @CsvBindByName(column = "username")
    @Column(name = "username", nullable = false, length = 50)
    @Indexed
    private String username;

    @CsvBindByName(column = "password")
    @Column(name = "userpass", length = 128)
    private String password;

    @CsvBindByName(column = "firstname")
    @Column(name = "firstname", length = 50)
    private String firstName;

    @CsvBindByName(column = "lastname")
    @Column(name = "lastname", length = 50)
    private String lastName;

    @CsvBindByName(column = "info")
    @Column(name = "info")
    private String info;

    @CsvBindByName(column = "enabled")
    @Column(name = "enabled")
    private int enabled;

    @Transient
    private boolean accountExpired;

    @Transient
    private boolean accountLocked;

    @Transient
    private boolean credentialExpired;

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public UsersEntity() {
        // new entity doesn't need to initialize values.  But, there are constraints of not null on username and uuid
    }

    public UsersEntity(String username, String password) {
        this.uid = UUID.randomUUID();
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersEntity that = (UsersEntity) o;
        return accountExpired == that.accountExpired && accountLocked == that.accountLocked && credentialExpired == that.credentialExpired && enabled == that.enabled && Objects.equals(uid, that.uid) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, username, password, firstName, lastName, info, accountExpired, accountLocked, credentialExpired, enabled);
    }

    @Override
    public String toString() {
        return "UsersEntity{" +
                "uid=" + uid +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", info='" + info + '\'' +
                ", accountExpired=" + accountExpired +
                ", accountLocked=" + accountLocked +
                ", credentialExpired=" + credentialExpired +
                ", enabled=" + enabled +
                '}';
    }

    public UUID getId() {
        return uid;
    }

    public void setId(UUID uid) {
        this.uid = uid;
    }

    /**
     * Returns the miscellaneous information about this user.
     *
     * @return the information
     */
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Returns the registered last name (surname) of the user
     *
     * @return the user's surname
     */
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    /**
     * Returns the registered first name (given name) of the user
     *
     * @return the user's given name
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the username used to authenticate the user. Cannot return
     * <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialExpired;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.enabled == 1;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled ? 1 : 0;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}