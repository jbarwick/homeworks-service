package com.jvj28.homeworks.command;

public class KeypadButtonHold implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBH.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBH.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
