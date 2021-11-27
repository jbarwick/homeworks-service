package com.jvj28.homeworks.command;

public class KeypadButtonDoubleTap implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBDT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBDT.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
