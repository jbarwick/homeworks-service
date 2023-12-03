package com.jvj28.homeworks.auth.contract;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ApiAuthRequest implements Serializable {

    private static final long serialVersionUID = 5317471391883683157L;

    @NonNull
    private String username;

    @NonNull
    private String password;
}
