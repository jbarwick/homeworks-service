package com.jvj28.homeworks.command;

public class SetSystemdDate implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SD.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SD.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
