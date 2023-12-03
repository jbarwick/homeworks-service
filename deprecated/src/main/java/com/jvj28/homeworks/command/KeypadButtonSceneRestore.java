package com.jvj28.homeworks.command;

public class KeypadButtonSceneRestore implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBSR.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBSR.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
