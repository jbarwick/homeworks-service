package com.jvj28.homeworks.command;

import lombok.Data;

import static com.jvj28.homeworks.command.Cmd.OSREV;

@Data
public class OSRevision implements HomeworksCommand {

    private String processorId;
    private String revision;
    private String model;

    @Override
    public String getName() {
        return OSREV.name();
    }

    @Override
    public String getCommand() {
        return OSREV.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line != null && line.length() > 0) {
            String[] parts = line.split(" ");
            if (parts.length > 1)
                this.processorId = parts[1];
            if (parts.length > 5)
                this.revision = parts[5];
            if (parts.length > 6)
                this.model = parts[6];
        }
    }
}
