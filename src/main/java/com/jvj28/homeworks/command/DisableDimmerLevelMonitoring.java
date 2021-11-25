package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableDimmerLevelMonitoring implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.DLMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DLMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Dimmer level monitoring disabled".equals(line))
            this.disabled = true;
    }
}
