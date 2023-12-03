package com.jvj28.homeworks.command;

public class GrafikEyeSceneSelect implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.GSS.name();
    }

    @Override
    public String getCommand() {
        return Cmd.GSS.toString();
    }

    @Override
    public void parseLine(String line) {

        // I don't know how to do this yet

    }
}
