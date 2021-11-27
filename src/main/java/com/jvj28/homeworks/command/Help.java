package com.jvj28.homeworks.command;

import lombok.Data;

import java.util.Arrays;

import static com.jvj28.homeworks.command.Cmd.HELP;

@Data
public class Help implements HomeworksCommand {

    private String response;

    @Override
    public String getName() {
        return HELP.name();
    }

    @Override
    public String getCommand() {
        return HELP.toString();
    }

    @Override
    public void parseLine(String line) {
        if (response == null) {
            response = line;
        } else {
            response = response + "\n" + line;
        }
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"Commands\": [");
        Arrays.stream(Cmd.values()).forEach(c -> {
            if (sb.length()>0)
                sb.append(',');
            sb.append(c.toJson());
        });
        sb.append("]}");
        return sb.toString();
    }
}
