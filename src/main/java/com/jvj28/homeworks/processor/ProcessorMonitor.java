package com.jvj28.homeworks.processor;

public interface ProcessorMonitor {

    void parseLine(String line);

    void setEnabled(boolean enable);

    boolean isEnabled();
}
