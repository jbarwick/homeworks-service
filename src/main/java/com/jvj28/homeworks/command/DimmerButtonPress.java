package com.jvj28.homeworks.command;

public class DimmerButtonPress implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DBP.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DBP.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
