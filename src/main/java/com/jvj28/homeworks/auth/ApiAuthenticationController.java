package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.auth.contract.ApiAuthRequest;
import com.jvj28.homeworks.auth.contract.ApiAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiAuthenticationController {

    private final Logger log = LoggerFactory.getLogger(ApiAuthenticationController.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final ApiAuthUserDetailsService apiUserDetailsService;
    private final AuthenticationManager authenticationManager;

    public ApiAuthenticationController(JwtTokenUtil jwtTokenUtil,
                                       ApiAuthUserDetailsService apiUserDetailsService, AuthenticationManager authenticationManager) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.apiUserDetailsService = apiUserDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/authenticate")
    public ApiAuthResponse createAuthenticationToken(@RequestBody ApiAuthRequest request) {
        Thread.currentThread().setName("/authenticate");

        String username = request.getUsername();
        String password = request.getPassword();

        log.info("Attempting login for user [{}]", username);
        log.debug("With password [{}]", password);

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        final UserDetails userDetails = apiUserDetailsService
                .loadUserByUsername(request.getUsername());

        return new ApiAuthResponse(jwtTokenUtil.generateToken(userDetails));
    }
}
