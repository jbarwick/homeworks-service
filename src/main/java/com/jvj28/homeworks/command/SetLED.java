package com.jvj28.homeworks.command;

public class SetLED implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SETLED.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SETLED.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
