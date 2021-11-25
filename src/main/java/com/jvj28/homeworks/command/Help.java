package com.jvj28.homeworks.command;

import lombok.Data;

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
        int z = 0;
        for (Cmd c : Cmd.values()) {
            if (z == 1) sb.append(','); else z = 1;
            sb.append(c.toJson());
        }
        sb.append("]}");
        return sb.toString();
    }
}
