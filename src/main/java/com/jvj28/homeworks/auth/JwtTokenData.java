package com.jvj28.homeworks.auth;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class JwtTokenData implements Serializable {

    private static final long serialVersionUID = 8733015036749179327L;

    @NonNull
    private String token;

}