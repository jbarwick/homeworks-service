package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class VacationModeCheck implements HomeworksCommand {

    private String devices;

    @Override
    public String getName() {
        return Cmd.VRS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.VRS.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("No Vacation Devices set-up".equals(line))
            this.devices = "No Devices";
    }
}
