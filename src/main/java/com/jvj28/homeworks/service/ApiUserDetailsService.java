package com.jvj28.homeworks.service;

import com.jvj28.homeworks.model.db.UsersEntityRepository;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class ApiUserDetailsService implements UserDetailsService {

    private final UsersEntityRepository users;

    public ApiUserDetailsService(UsersEntityRepository users) {
        this.users = users;
    }

    public boolean authenticate(String username, String password) {
        return users.findByUserName(username).map(usersEntity -> usersEntity.getUserPass().equals(password)).orElse(false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
        Optional<UsersEntity> u = users.findByUserName(username);
        if (u.isPresent()) {
            return new User(username, u.get().getUserPass(), authorities);
        } else {
            throw new UsernameNotFoundException(String.format("User [%s] not found", username));
        }
    }
}
