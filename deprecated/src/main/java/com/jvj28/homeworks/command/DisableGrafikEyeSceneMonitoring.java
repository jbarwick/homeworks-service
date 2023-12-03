package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class DisableGrafikEyeSceneMonitoring implements HomeworksCommand {

    private boolean disabled;

    @Override
    public String getName() {
        return Cmd.GSMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.GSMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("GrafikEye scene monitoring disabled".equals(line))
            this.disabled = true;
    }
}
