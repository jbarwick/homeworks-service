package com.jvj28.homeworks.command;

public class StopDimmer implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.STOPDIM.name();
    }

    @Override
    public String getCommand() {
        return Cmd.STOPDIM.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
