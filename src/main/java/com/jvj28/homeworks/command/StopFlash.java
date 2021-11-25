package com.jvj28.homeworks.command;

public class StopFlash implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.STOPFLASH.name();
    }

    @Override
    public String getCommand() {
        return Cmd.STOPFLASH.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
