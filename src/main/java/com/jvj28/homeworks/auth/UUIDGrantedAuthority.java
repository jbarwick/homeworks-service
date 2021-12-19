package com.jvj28.homeworks.auth;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Data
public class UUIDGrantedAuthority implements GrantedAuthority {

    private UUID userId;

    public UUIDGrantedAuthority(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String getAuthority() {
        return userId.toString();
    }
}
