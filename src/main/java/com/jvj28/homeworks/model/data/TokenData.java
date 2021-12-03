package com.jvj28.homeworks.model.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class TokenData implements Serializable {

    @Serial
    private static final long serialVersionUID = 8733015036749179327L;

    @NonNull
    private String token;

}