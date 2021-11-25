package com.jvj28.homeworks.command;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.jvj28.homeworks.command.Cmd.LOGIN;

@Data
@RequiredArgsConstructor
public class Login implements HomeworksCommand {

    @NonNull
    private final String username;

    @NonNull
    private final String password;

    private boolean succeeded = false;

    @Override
    public String getName() {
        return LOGIN.name();
    }

    @Override
    public String getCommand() {
        return String.format("%s, %s", username, password);
    }

    @Override
    public void parseLine(String line) {
        if ("login successful".equals(line)) {
            this.succeeded = true;
        }
    }
}
