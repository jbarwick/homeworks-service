package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class RequestAllProcessorStatusInformation implements HomeworksCommand {

    private String processorInfo;

    @Override
    public String getName() {
        return Cmd.PINFO.name();
    }

    @Override
    public String getCommand() {
        return Cmd.PINFO.toString();
    }

    /**
     * This returns something like: "P00164002C0D2000000310T"
     * @param line String of data (a line) returned by the processor
     */
    @Override
    public void parseLine(String line) {
        this.processorInfo = line;
    }
}
