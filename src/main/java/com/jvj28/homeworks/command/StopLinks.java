package com.jvj28.homeworks.command;

public class StopLinks implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.STOPLNKS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.STOPLNKS.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
