package com.jvj28.homeworks.service;

public interface HomeworksMonitor {

    void parseLine(String line);

    void setEnabled(boolean enable);

    boolean isEnabled();
}
