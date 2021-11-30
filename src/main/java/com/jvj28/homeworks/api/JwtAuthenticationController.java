package com.jvj28.homeworks.api;

import com.jvj28.homeworks.components.JwtTokenUtil;
import com.jvj28.homeworks.data.model.TokenData;
import com.jvj28.homeworks.data.model.TokenRequestData;
import com.jvj28.homeworks.service.ApiUserDetailsService;
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
public class JwtAuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final ApiUserDetailsService apiUserDetailsService;

    public JwtAuthenticationController(AuthenticationManager authenticationManager,
                                       JwtTokenUtil jwtTokenUtil,
                                       ApiUserDetailsService apiUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.apiUserDetailsService = apiUserDetailsService;
    }

    @PostMapping(value = "/authenticate")
    public TokenData createAuthenticationToken(@RequestBody TokenRequestData authenticationRequest) {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = apiUserDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        return new TokenData(jwtTokenUtil.generateToken(userDetails));
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
