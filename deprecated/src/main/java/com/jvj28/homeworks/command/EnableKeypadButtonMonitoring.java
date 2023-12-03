package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableKeypadButtonMonitoring implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.KBMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBMON.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Keypad button monitoring enabled".equals(line))
            this.enabled = true;
    }
}
