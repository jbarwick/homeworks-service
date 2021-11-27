package com.jvj28.homeworks.command;

public class GrxZoneFlash implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.GRXFLASH.name();
    }

    @Override
    public String getCommand() {
        return Cmd.GRXFLASH.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
