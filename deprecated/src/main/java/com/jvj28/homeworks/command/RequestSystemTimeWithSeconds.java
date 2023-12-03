package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestSystemTimeWithSeconds implements HomeworksCommand {

    private String time;

    @Override
    public String getName() {
        return Cmd.RST2.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RST2.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0) return;
        int idx = line.indexOf("Processor Time: ");
        if (idx>0) this.time = line.substring(idx);
    }
}
