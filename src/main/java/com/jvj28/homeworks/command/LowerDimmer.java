package com.jvj28.homeworks.command;

public class LowerDimmer implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.LOWERDIM.name();
    }

    @Override
    public String getCommand() {
        return Cmd.LOWERDIM.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
