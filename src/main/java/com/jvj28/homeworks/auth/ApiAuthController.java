package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.auth.contract.ApiAuthRequest;
import com.jvj28.homeworks.auth.contract.ApiAuthResponse;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@RestController
public class ApiAuthController {

    private final Logger log = LoggerFactory.getLogger(ApiAuthController.class);

    private final ApiAuthControllerService service;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public ApiAuthController(ApiAuthControllerService apiUserDetailsService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.service = apiUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/refresh")
    public ApiAuthResponse refreshToken() {
        Thread.currentThread().setName("/refresh");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getAuthenticationResponse(authentication);
    }

    @PostMapping("/authenticate")
    public ApiAuthResponse createAuthenticationToken(@RequestBody ApiAuthRequest request) {
        Thread.currentThread().setName("/authenticate");

        String username = request.getUsername();
        String password = request.getPassword();

        log.info("Attempting login for user [{}]", username);
        log.debug("With password [{}]", password);

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return getAuthenticationResponse(authentication);
    }

    private ApiAuthResponse getAuthenticationResponse(Authentication authentication) {
        if (authentication == null)
            throw new AuthenticationServiceException("Service Authentication Failed");

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UsersEntity))
            throw new AuthenticationServiceException("Principal is empty or invalid");
        
        UsersEntity u = (UsersEntity) principal;

        String token = jwtTokenUtil.generateToken(u);
        Date expiry = jwtTokenUtil.getExpirationDateFromToken(token);
        u.setCredentialsExpires(LocalDateTime.ofInstant(expiry.toInstant(), ZoneId.systemDefault()));
        service.saveUserDetails(u);
        ApiAuthResponse response = new ApiAuthResponse();
        response.setToken(token);
        response.setExpires(ZonedDateTime.of(u.getCredentialsExpires(), ZoneId.systemDefault()));
        return response;
    }
}
