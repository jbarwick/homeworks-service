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
public class ApiAuthControllerService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(ApiAuthControllerService.class);

    private final Model model;

    public ApiAuthControllerService(Model model) {
        this.model = model;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsername(username, false);
    }

    public UserDetails loadUserByUsername(String username, boolean cached) {
        UsersEntity user = this.model.getUserByUsername(username, cached);
        if (user == null)
            throw new UsernameNotFoundException(String.format("User [%s] not found", username));
        if (log.isDebugEnabled())
            log.debug(user.toString());
        return user;
    }

    public void saveUserDetails(UserDetails userDetails) {
        if (userDetails instanceof UsersEntity) {
            this.model.saveUserToCache((UsersEntity)userDetails);
        }
    }
}
