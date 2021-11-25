package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class TodaysSunrise implements HomeworksCommand {

    String time;

    @Override
    public String getName() {
        return Cmd.SUNRISE.name();
    }

    @Override
    public String getCommand() {
        return Cmd.SUNRISE.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0) return;
        int idx = line.indexOf("Today's Sunrise: ");
        if (idx > 0) this.time = line.substring(idx);
    }
}
