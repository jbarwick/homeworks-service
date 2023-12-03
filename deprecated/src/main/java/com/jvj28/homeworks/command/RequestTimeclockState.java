package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestTimeclockState implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.TCS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.TCS.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("All Timeclocks Disabled".equals(line))
            this.enabled = false;
        if ("All Timeclocks Enabled".equals(line))
            this.enabled = true;
    }
}
