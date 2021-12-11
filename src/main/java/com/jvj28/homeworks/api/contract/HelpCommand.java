package com.jvj28.homeworks.api.contract;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class HelpCommand implements Serializable {

    private static final long serialVersionUID = 2453801219256378004L;

    private String name;
    private String description;

    public HelpCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
