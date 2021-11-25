package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableTimeclock implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.TCE.name();
    }

    @Override
    public String getCommand() {
        return Cmd.TCE.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("TCE: All Timeclocks Enabled".equals(line))
            this.enabled = true;
    }
}
