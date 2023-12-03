package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class Logout implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return Cmd.LOGOUT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.LOGOUT.toString();
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
