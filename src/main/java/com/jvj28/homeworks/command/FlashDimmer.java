package com.jvj28.homeworks.command;

public class FlashDimmer implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.FLASHDIM.name();
    }

    @Override
    public String getCommand() {
        return Cmd.FLASHDIM.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
