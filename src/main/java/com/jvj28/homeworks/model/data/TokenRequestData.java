package com.jvj28.homeworks.model.data;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TokenRequestData implements Serializable {

    @Serial
    private static final long serialVersionUID = 5317471391883683157L;

    private String username;

    private String password;

}
