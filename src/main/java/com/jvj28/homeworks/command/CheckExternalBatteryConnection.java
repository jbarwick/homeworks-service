package com.jvj28.homeworks.command;

public class CheckExternalBatteryConnection implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.CHKBATT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.CHKBATT.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
