package com.jvj28.homeworks.command;

public class SetSystemTime implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.ST.name();
    }

    @Override
    public String getCommand() {
        return Cmd.ST.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
