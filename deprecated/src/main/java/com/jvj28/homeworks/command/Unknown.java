package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class Unknown implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return "UNKNOWN";
    }

    @Override
    public String getCommand() {
        return "UNKNOWN";
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
