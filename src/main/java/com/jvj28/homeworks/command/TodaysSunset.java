package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class TodaysSunset implements HomeworksCommand {

    String time;

    @Override
    public String getName() {
        return Cmd.SUNSET.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SUNSET.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0) return;
        int idx = line.indexOf("Today's Sunset: ");
        if (idx > 0) this.time = line.substring(idx);
    }
}
