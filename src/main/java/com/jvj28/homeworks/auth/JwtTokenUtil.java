package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.model.db.entity.UsersEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
@ConfigurationProperties(prefix="homeworks.api.encryption")
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final int JWT_TOKEN_VALIDITY_SECONDS = 20 * 60;  // 20 minutes
    public static final String ISSUER = "Homeworks Service";

    @Value("${homeworks.api.encryption.secret}")
    private String secret;

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public UUID getUUIDFromToken(String token) {
        String value = getClaimFromToken(token, Claims::getId);
        return value == null ? null : UUID.fromString(value);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    //check if the token has expired
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    //generate token for user

    /**
     * This function mutates userDetails by setting the CredentialsExpires
     * @param userDetails mutated UsersEntity (if a UsersEntity)
     * @return the JWT Token String
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Date expiry = new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY_SECONDS * 1000L);
        UUID uuid = null;
        if (userDetails instanceof UsersEntity) {
            uuid = ((UsersEntity)userDetails).getUid();
        }
        return doGenerateToken(claims, uuid, userDetails.getUsername(), expiry);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, UUID uuid,  String subject, Date expiry) {

        return Jwts.builder().setClaims(claims)
                .setIssuer(ISSUER)
                .setSubject(subject)
                .setId(uuid == null ? null : uuid.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public boolean validateSubject(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(getUsernameFromToken(token));
    }

    public boolean validateUUID(String token, UUID uuid) {
        return uuid.equals(getUUIDFromToken(token));
    }

    public boolean isValidIssuer(String token) {
        String issuer = getIssuerFromToken(token);
        return ISSUER.equals(issuer);
    }

    public String getIssuerFromToken(String token) {
        return getClaimFromToken(token,  Claims::getIssuer);
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
