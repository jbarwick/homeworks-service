package com.jvj28.homeworks.command;

public class KeypadButtonRelease implements HomeworksCommand {
    @Override
    public String getName() {
        return Cmd.KBR.name();
    }

    @Override
    public String getCommand() {
        return Cmd.KBR.toString();
    }

    @Override
    public void parseLine(String line) {

    }}
