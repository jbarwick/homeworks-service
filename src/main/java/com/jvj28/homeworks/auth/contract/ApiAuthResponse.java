package com.jvj28.homeworks.auth.contract;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class ApiAuthResponse implements Serializable {

    private static final long serialVersionUID = 8733015036749179327L;

    @NonNull
    private String token;

}
