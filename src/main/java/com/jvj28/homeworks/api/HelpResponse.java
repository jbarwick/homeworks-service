package com.jvj28.homeworks.api;

import com.jvj28.homeworks.command.Cmd;
import lombok.Data;

import java.util.ArrayList;

@Data
public class HelpResponse {

    ArrayList<HelpCommandListEntry> commands = new ArrayList<>();

    public HelpResponse() {
        Cmd.stream().forEach(c -> commands.add(new HelpCommandListEntry(c.name(), c.description())));
    }
}
