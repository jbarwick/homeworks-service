package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestBootRevisions implements HomeworksCommand {

    private String processorId;
    private String bootRevision;

    @Override
    public String getName() {
        return Cmd.BOOTREV.name();
    }

    @Override
    public String getCommand() {
        return Cmd.BOOTREV.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line!=null && line.length() > 0) {
            String[] parts = line.split(" ");
            if (parts.length>=6) {
                this.processorId = parts[1];
                this.bootRevision = parts[5];
            }
        }
    }
}
