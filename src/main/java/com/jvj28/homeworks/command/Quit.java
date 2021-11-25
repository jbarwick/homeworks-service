package com.jvj28.homeworks.command;

public class Quit implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.QUIT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.QUIT.toString();
    }

    @Override
    public void parseLine(String line) {

    }
}
