package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestLinkShortStatus implements HomeworksCommand {

    private String linkStatus;
    private String processorId;

    @Override
    public String getName() {
        return Cmd.LNKSHRT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.LNKSHRT.toString();
    }

    @Override
    public void parseLine(String line) {
        // Processor 01: POWERED LINKS - OK
        if (line==null || line.length()==0) return;
        String [] parts = line.split(":");
        this.processorId = parts[0].split(" ")[1];
        this.linkStatus = parts[1].trim();
    }
}
