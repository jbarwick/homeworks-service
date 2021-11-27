package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class Ping implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return Cmd.PING.name();
    }

    @Override
    public String getCommand() {
        return Cmd.PING.toString();
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
