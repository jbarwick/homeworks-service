package com.jvj28.homeworks.auth;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.StringUtils.hasText;

@Component
public class ApiRequestAuthorizationFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(ApiRequestAuthorizationFilter.class);

    private final ApiAuthControllerService apiAuthService;
    private final JwtTokenUtil jwtTokenUtil;

    public ApiRequestAuthorizationFilter(ApiAuthControllerService jwtApiUserDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.apiAuthService = jwtApiUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null)
            doAttemptAuthentication(request);
        chain.doFilter(request, response);
    }

    private void doAttemptAuthentication(HttpServletRequest request) {

        String authHeader = request.getHeader(AUTHORIZATION);

        // Silently exit if there is no authorization header
        if (!hasText(authHeader))
            return;

        try {
            // Get the token.  Will never be null. if the token is expired or has not been issued by the issuer
            // a JwtException will be thrown
            String token = getTokenFrom(authHeader);

            String username = jwtTokenUtil.getUsernameFromToken(token);

            // if the username is not in this token, then let's silently exit
            if (username == null)
                return;

            // Let's get all the user details from the auth controller
            UserDetails userDetails = this.apiAuthService.loadUserByUsername(username, true);

            // if token is valid configure Spring Security to manually set authentication
            if (userDetails.isEnabled() && userDetails.isAccountNonExpired() &&
                    userDetails.isAccountNonLocked() && userDetails.isCredentialsNonExpired() &&
                    this.jwtTokenUtil.validateSubject(token, userDetails)) {
                if (userDetails instanceof CredentialsContainer) {
                    ((CredentialsContainer) userDetails).eraseCredentials();
                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (JwtException je) {
            log.error("Token Error: {}", je.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Unable retrieve token from request header");
        } catch (UsernameNotFoundException unfe) {
            log.error("Cannot authenticate as the user information was not found");
        }
    }

    @NonNull
    private String getTokenFrom(String authHeader) throws JwtException {
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Bearer not specified in authorization header");
        String token = authHeader.substring(7);
        if (!jwtTokenUtil.isValidIssuer(token))
            throw new JwtException("Issuer does not match");
        if (jwtTokenUtil.isTokenExpired(token))
            throw new JwtException("Token is expired");
        return token;
    }

}
