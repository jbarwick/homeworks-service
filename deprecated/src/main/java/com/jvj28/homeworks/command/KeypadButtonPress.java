package com.jvj28.homeworks.command;

public class KeypadButtonPress implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBP.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBP.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
