package com.jvj28.homeworks.auth.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
public class ApiAuthResponse implements Serializable {

    private static final long serialVersionUID = 8733015036749179327L;

    private String token;

    @JsonInclude(NON_NULL)
    private ZonedDateTime expires;

    @JsonInclude(NON_NULL)
    private ZonedDateTime lockedUntil;
}
