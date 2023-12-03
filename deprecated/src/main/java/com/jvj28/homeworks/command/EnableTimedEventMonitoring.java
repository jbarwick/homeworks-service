package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableTimedEventMonitoring implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.TEMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.TEMON.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Timed Event monitoring enabled".equals(line))
            this.enabled = true;
    }
}
