package com.jvj28.homeworks.command;

public class RaiseDimmer implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.RAISEDIM.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RAISEDIM.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
