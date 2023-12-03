package com.jvj28.homeworks.command;

public class StartLinks implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.STARTLNKS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.STARTLNKS.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
