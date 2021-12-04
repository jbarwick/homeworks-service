package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.api.BadRequestException;
import com.jvj28.homeworks.api.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ApiAuthenticationController {

    private final Logger log = LoggerFactory.getLogger(ApiAuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final ApiAuthUserDetailsService apiUserDetailsService;

    public ApiAuthenticationController(AuthenticationManager authenticationManager,
                                       JwtTokenUtil jwtTokenUtil,
                                       ApiAuthUserDetailsService apiUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.apiUserDetailsService = apiUserDetailsService;
    }

    @PostMapping(value = "/authenticate")
    public String createAuthenticationToken(@RequestBody ApiAuthRequestData request) {
        Thread.currentThread().setName("/authenticate");

        String username = request.getUsername();
        String password = request.getPassword();

        log.info("Attempting login for user [{}]", username);
        log.debug("With password [{}]", password);

        authenticate(username, password);

        final UserDetails userDetails = apiUserDetailsService
                .loadUserByUsername(request.getUsername());

        return jwtTokenUtil.generateToken(userDetails);
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new BadRequestException("USER_DISABLED");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("INVALID_CREDENTIALS");
        }
    }
}
