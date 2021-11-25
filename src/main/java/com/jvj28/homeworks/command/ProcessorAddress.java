package com.jvj28.homeworks.command;

import lombok.Data;

import static com.jvj28.homeworks.command.Cmd.PROCADDR;

@Data
public class ProcessorAddress implements HomeworksCommand {

    private String processorAddress;
    private String mode;

    @Override
    public String getName() {
        return PROCADDR.name();
    }

    @Override
    public String getCommand() {
        return PROCADDR.toString();
    }

    @Override
    public void parseLine(String line) {
        //Processor Address : 01 in OS mode
        if (line != null && line.length() > 0) {
            String[] parts = line.split(" ");
            if (parts.length>3)
                processorAddress=parts[3];
            if (parts.length>5)
                mode=parts[5];
        }
    }
}
