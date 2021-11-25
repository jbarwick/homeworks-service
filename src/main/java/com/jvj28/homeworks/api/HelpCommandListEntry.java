package com.jvj28.homeworks.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HelpCommandListEntry {

    String name;

    String description;

    public HelpCommandListEntry(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
