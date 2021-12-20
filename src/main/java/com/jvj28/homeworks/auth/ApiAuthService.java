package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ApiAuthService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(ApiAuthService.class);

    private final Model model;

    public ApiAuthService(Model model) {
        this.model = model;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity user = this.model.getUserByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(String.format("User [%s] not found", username));
        if (log.isDebugEnabled())
            log.debug(user.toString());
        return user;
    }
}
