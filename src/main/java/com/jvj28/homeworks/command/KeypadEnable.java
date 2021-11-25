package com.jvj28.homeworks.command;

public class KeypadEnable implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KE.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KE.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
