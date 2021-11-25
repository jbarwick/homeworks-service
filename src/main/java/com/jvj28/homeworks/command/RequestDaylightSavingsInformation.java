package com.jvj28.homeworks.command;

public class RequestDaylightSavingsInformation implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.RDS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RDS.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
