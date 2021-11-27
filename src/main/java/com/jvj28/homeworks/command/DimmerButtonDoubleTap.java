package com.jvj28.homeworks.command;

public class DimmerButtonDoubleTap implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DBDT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DBDT.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
