package com.jvj28.homeworks.command;

public class SetLEDs implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SETLEDS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SETLEDS.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
