package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableKeypadButtonMonitoring implements HomeworksCommand {

    private boolean disabled = false;

    @Override
    public String getName() {
        return Cmd.KBMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Keypad button monitoring disabled".equals(line))
            this.disabled = true;
    }
}
