package com.jvj28.homeworks.command;

public class DimmerButtonHold implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DBH.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DBH.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
