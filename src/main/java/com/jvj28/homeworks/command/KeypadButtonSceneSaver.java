package com.jvj28.homeworks.command;

public class KeypadButtonSceneSaver implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBSS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBSS.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
