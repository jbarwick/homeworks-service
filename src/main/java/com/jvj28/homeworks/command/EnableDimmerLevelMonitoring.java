package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableDimmerLevelMonitoring implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.DLMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DLMON.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Timed Event monitoring enabled".equals(line))
            this.enabled = true;
    }
}
