package com.jvj28.homeworks.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiAuthRequestData implements Serializable {

    private static final long serialVersionUID = 5317471391883683157L;

    private String username;
    private String password;
}
