package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ApiAuthUserDetailsService implements UserDetailsService {

    private final Model model;

    public ApiAuthUserDetailsService(Model model) {
        this.model = model;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity user = model.getUserByUsername(username);
        if (user != null) {
            return new User(username, user.getUserPass(), new ArrayList<>());
        } else {
            throw new UsernameNotFoundException(String.format("User [%s] not found", username));
        }
    }
}
