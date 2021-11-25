package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableKeypadLEDMonitoring implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.KLMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KLMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Keypad led monitoring enabled".equals(line))
            this.enabled = true;
    }
}
