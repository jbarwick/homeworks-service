package com.jvj28.homeworks.command;

public class SetDHCP implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.SETDHCP.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SETDHCP.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
