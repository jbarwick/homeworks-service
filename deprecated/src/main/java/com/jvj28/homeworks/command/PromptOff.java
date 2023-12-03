package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class PromptOff implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return Cmd.PROMPTOFF.name();
    }

    @Override
    public String getCommand() {
        return Cmd.PROMPTOFF.toString();
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
