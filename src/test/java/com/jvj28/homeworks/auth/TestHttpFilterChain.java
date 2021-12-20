package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestHttpFilterChain {

    @MockBean
    private ApiAuthService authControllerService;

    private UsersEntity userDetails;
    private JwtTokenUtil jwtTokenUtil;
    private String token;

    @BeforeEach
    public void generateUserDetails() {
        this.userDetails = new UsersEntity();
        this.userDetails.setId(UUID.randomUUID());
        this.userDetails.setUsername("test-user");
        this.userDetails.setPassword(new BCryptPasswordEncoder().encode("sample-password"));
        this.userDetails.setFirstName("Test");
        this.userDetails.setLastName("User");
        this.userDetails.setEnabled(true);
        this.userDetails.setInfo("Other Information");

        this.jwtTokenUtil = new JwtTokenUtil();
        this.jwtTokenUtil.setSecret("master password");
        this.token = this.jwtTokenUtil.generateToken(this.userDetails);
        assertNotNull(this.token);

        authControllerService = mock(ApiAuthService.class);
    }

    @Test
    void testApiRequestAuthorizationFilter() {

        try {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + this.token);

            String username = this.userDetails.getUsername();
            when(authControllerService.loadUserByUsername(username)).thenReturn(this.userDetails);

            ApiRequestAuthorizationFilter filter = new ApiRequestAuthorizationFilter(authControllerService, this.jwtTokenUtil);
            filter.doFilterInternal(request, response, chain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);

            Object principal = authentication.getPrincipal();
            assertTrue(principal instanceof UsersEntity);

            assertEquals(this.userDetails, principal);

        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
    }
}
