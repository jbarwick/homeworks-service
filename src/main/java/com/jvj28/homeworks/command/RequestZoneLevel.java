package com.jvj28.homeworks.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class RequestZoneLevel implements HomeworksCommand {

    @NonNull
    private String address;

    private int level;

    @Override
    public String getName() {
        return Cmd.RDL.name();
    }

    @Override
    public String getCommand() {
        return Cmd.RDL.name() + ",[" + address + "]";
    }

    @Override
    public void parseLine(String line) {
        if (line == null || !line.startsWith("DL,"))
            return;

        String [] parts = line.split(",");
        this.address = parts[1].substring(parts[1].indexOf("[") + 1, parts[1].indexOf("]"));
        try {
            this.level = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException nfe) {
            this.level = 0;
        }
    }
}
