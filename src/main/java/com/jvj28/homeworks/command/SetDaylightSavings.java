package com.jvj28.homeworks.command;

public class SetDaylightSavings implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SDS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SDS.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
