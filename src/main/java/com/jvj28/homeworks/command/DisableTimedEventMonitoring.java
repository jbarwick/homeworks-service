package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableTimedEventMonitoring implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.TEMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.TEMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Timed Event monitoring disabled".equals(line))
            this.disabled = true;
    }
}
