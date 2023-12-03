package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestVacationModeState implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.VMS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.VMS.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Vacation mode disabled".equals(line))
            this.enabled = false;
        else if ("Vacation mode enabled".equals(line))
            this.enabled = true;
    }
}
