package com.jvj28.homeworks.command;

public class VacationModePlayback implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.VMP.name();
    }

    @Override
    public String getCommand() {
        return Cmd.VMP.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
