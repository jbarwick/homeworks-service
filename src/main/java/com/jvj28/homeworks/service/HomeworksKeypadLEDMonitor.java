package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.DisableKeypadLEDMonitoring;
import com.jvj28.homeworks.command.EnableKeypadLEDMonitoring;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HomeworksKeypadLEDMonitor implements HomeworksMonitor {

    private final Logger log = LoggerFactory.getLogger(HomeworksKeypadLEDMonitor.class);

    private final HomeworksProcessor processor;
    private boolean enabled;

    public HomeworksKeypadLEDMonitor(HomeworksProcessor processor) {
        this.processor = processor;
        this.processor.addKeypadLEDMonitor(this);
    }

    @Override
    public void parseLine(String line) {
        if (!isEnabled() || Strings.isEmpty(line) || !line.startsWith("KL,"))
            return;

        log.debug("Keypad LED Received: " + line);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            processor.sendCommand(EnableKeypadLEDMonitoring.class);
        else
            processor.sendCommand(DisableKeypadLEDMonitoring.class);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
