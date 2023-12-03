package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.model.db.entity.UsersEntity;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TestJwtTokenUtil {

    private UsersEntity userDetails;
    private JwtTokenUtil jwtTokenUtil;
    private String token;

    @BeforeEach
    public void generateUserDetails() {
        this.userDetails = new UsersEntity();
        this.userDetails.setUid(UUID.randomUUID());
        this.userDetails.setUsername("test-user");
        this.userDetails.setPassword(new BCryptPasswordEncoder().encode("sample-password"));
        this.userDetails.setFirstName("Test");
        this.userDetails.setLastName("User");
        this.userDetails.setEnabled(1);
        this.userDetails.setInfo("Other Information");

        this.jwtTokenUtil = new JwtTokenUtil();
        this.jwtTokenUtil.setSecret("master password");
        this.token = this.jwtTokenUtil.generateToken(this.userDetails);
        assertNotNull(this.token);
    }

    @Test
    void getUsernameFromToken() {
        String username = this.jwtTokenUtil.getUsernameFromToken(this.token);
        assertEquals(this.userDetails.getUsername(), username);
    }

    @Test
    void getExpirationDateFromToken() throws InterruptedException {
        Date expirationTest = new Date(System.currentTimeMillis() + JwtTokenUtil.JWT_TOKEN_VALIDITY_SECONDS * 1000L);
        Date expirationDate = this.jwtTokenUtil.getExpirationDateFromToken(this.token);
        TimeUnit.SECONDS.sleep(1);
        assertTrue(expirationDate.after(new Date()));
        assertTrue(expirationDate.before(expirationTest));
    }

    @Test
    void getClaimFromToken() {
        String subject = this.jwtTokenUtil.getClaimFromToken(this.token, Claims::getSubject);
        System.out.println("Validate Subject: " + subject);
        assertEquals(this.userDetails.getUsername(), subject);
        String id = this.jwtTokenUtil.getClaimFromToken(this.token, Claims::getId);
        System.out.println("Validate Id: " + id);
        assertEquals(this.userDetails.getUid().toString(), id);
        String issuer = this.jwtTokenUtil.getClaimFromToken(this.token, Claims::getIssuer);
        assertEquals("Homeworks Service", issuer);
    }

    @Test
    void validateToken() {
        boolean result = this.jwtTokenUtil.validateSubject(this.token, this.userDetails);
        assertTrue(result);
        result = this.jwtTokenUtil.validateUUID(this.token, this.userDetails.getUid());
        assertTrue(result);
    }
}