package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class Quit implements HomeworksCommand {

    private String response;

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
        this.response = line;
    }
}
