package com.jvj28.homeworks.command;

public class VacationModeRecord implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.VMR.name();
    }

    @Override
    public String getCommand() {
        return Cmd.VMR.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
