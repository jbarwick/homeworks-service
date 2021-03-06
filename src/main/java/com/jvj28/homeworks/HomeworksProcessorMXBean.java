package com.jvj28.homeworks;

public interface HomeworksProcessorMXBean {

    String getUsername();

    String getConsolePassword();

    String getConsoleHost();

    int getPort();

    int getQueueDepth();

    void startProcessor();

    void stopProcessor();
}
