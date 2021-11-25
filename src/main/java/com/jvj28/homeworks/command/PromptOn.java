package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class PromptOn implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return Cmd.PROMPTON.name();
    }

    @Override
    public String getCommand() {
        return Cmd.PROMPTON.toString();
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
