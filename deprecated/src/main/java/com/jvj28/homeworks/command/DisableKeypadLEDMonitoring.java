package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableKeypadLEDMonitoring implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.KLMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KLMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Keypad led monitoring disabled".equals(line))
            this.disabled = true;
    }
}
