package com.jvj28.homeworks.command;

public class Ping implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.PING.name();
    }

    @Override
    public String getCommand() {
        return Cmd.PING.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
