package com.jvj28.homeworks.command;

public class DimmerButtonRelease implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.DBR.name();
    }

    @Override
    public String getCommand() {
        return Cmd.DBR.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
