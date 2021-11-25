package com.jvj28.homeworks.command;

public class EnableDriverLevelMonitoring implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DRMON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DRMON.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
