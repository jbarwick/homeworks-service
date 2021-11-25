package com.jvj28.homeworks.command;

public class FadeDimmer implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.FADEDIM.name();
    }

    @Override
    public String getCommand() {
        return Cmd.FADEDIM.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
