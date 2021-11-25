package com.jvj28.homeworks.command;

import lombok.Data;

import static com.jvj28.homeworks.command.Cmd.REPLYON;

@Data
public class ReplyOn implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return REPLYON.name();
    }

    @Override
    public String getCommand() {
        return REPLYON.toString();
    }

    @Override
    public void parseLine(String line) {
        this.response = line;
    }
}
