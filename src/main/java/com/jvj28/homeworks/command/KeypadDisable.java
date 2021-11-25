package com.jvj28.homeworks.command;

public class KeypadDisable implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KD.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KD.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
