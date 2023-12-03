package com.jvj28.homeworks.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jvj28.homeworks.command.Cmd;
import com.jvj28.homeworks.model.data.CommandData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HelpResponse implements Serializable {

    private static final long serialVersionUID = 8272801524972110202L;

    private final List<CommandData> commands = new ArrayList<>();

    public HelpResponse() {
        Cmd.stream().forEach(c -> commands.add(new CommandData(c.name(), c.description())));
    }

    @JsonProperty("commands")
    public List<CommandData> getCommands() {
        return this.commands;
    }
}
