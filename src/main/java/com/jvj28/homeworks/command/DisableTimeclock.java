package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableTimeclock implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.TCD.name();
    }

    @Override
    public String getCommand() {
        return Cmd.TCD.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("TCD: Timeclock Disabled".equals(line))
            this.disabled = true;
    }
}
