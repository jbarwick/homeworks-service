package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestSystemDate implements HomeworksCommand {

    private String date;
    private String dayOfWeek;

    @Override
    public String getName() {
        return Cmd.RSD.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RSD.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0) return;
        String[] parts = line.split(" ");
        if (parts.length < 4) return;
        if ("Processor".equals(parts[0]) && "Date:".equals(parts[1])) {
            dayOfWeek = parts[2];
            date = parts[3];
        }
    }
}
