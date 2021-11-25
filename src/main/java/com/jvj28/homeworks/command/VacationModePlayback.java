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

    }
}
