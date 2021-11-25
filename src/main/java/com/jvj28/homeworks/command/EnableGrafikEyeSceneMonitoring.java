package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class EnableGrafikEyeSceneMonitoring implements HomeworksCommand {

    private boolean enabled;

    @Override
    public String getName() {
        return Cmd.GSMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.GSMOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        if ("GrafikEye scene monitoring enabled".equals(line))
            this.enabled = true;

    }
}
