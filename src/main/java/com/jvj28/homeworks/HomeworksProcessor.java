package com.jvj28.homeworks;

import com.jvj28.homeworks.processor.Processor;
import com.jvj28.homeworks.processor.ProcessorConfiguration;
import org.springframework.stereotype.Component;

@Component
public class HomeworksProcessor implements HomeworksProcessorMXBean {

    private final ProcessorConfiguration config;
    private final Processor processor;

    public HomeworksProcessor(ProcessorConfiguration config, Processor processor) {
        this.config = config;
        this.processor = processor;
    }

    @Override
    public String getUsername() {
        return config.getUsername();
    }

    @Override
    public String getConsolePassword() {
        return config.getConsolePassword();
    }

    @Override
    public String getConsoleHost() {
        return config.getConsoleHost();
    }

    @Override
    public int getPort() {
        return config.getPort();
    }

    @Override
    public int getQueueDepth() {
        return this.processor.getQueueSize();
    }

    public void startProcessor() {
        this.processor.start();
    }

    public void stopProcessor() {
        this.processor.stop();
    }
}
