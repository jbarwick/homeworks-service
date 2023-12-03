package com.jvj28.homeworks.command;

public class DisableDriverLevelMonitoring implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DRMOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DRMOFF.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
