package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class VacationModeDisable implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.VMD.name();
    }

    @Override
    public String getCommand() {
        return Cmd.VMD.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("Vacation Mode Disabled".equals(line))
            this.disabled = true;
    }
}
