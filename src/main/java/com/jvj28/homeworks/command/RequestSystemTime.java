package com.jvj28.homeworks.command;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class RequestSystemTime implements HomeworksCommand {

    private String time;

    @Override
    public String getName() {
        return Cmd.RST.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RST.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0) return;
        String[] parts = line.split(" ");
        if (parts.length < 3) return;
        if ("Processor".equals(parts[0]) && "Time:".equals(parts[1])) {
            time = parts[2];
        }
    }
}
