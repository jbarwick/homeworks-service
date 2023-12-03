package com.jvj28.homeworks.command;

public class SetPing implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SETPING.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SETPING.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
