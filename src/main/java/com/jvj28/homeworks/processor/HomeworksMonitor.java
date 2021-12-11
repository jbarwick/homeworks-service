package com.jvj28.homeworks.processor;

public interface HomeworksMonitor {

    void parseLine(String line);

    void setEnabled(boolean enable);

    boolean isEnabled();
}
