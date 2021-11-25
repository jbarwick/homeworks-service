package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.DisableKeypadButtonMonitoring;
import com.jvj28.homeworks.command.EnableKeypadButtonMonitoring;
import com.jvj28.homeworks.data.Model;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HomeworksKeypadButtonMonitor implements HomeworksMonitor {

    private final Logger log = LoggerFactory.getLogger(HomeworksKeypadButtonMonitor.class);

    private final HomeworksProcessor processor;
    private boolean enabled;

    public HomeworksKeypadButtonMonitor(HomeworksProcessor processor) {
        this.processor = processor;
        this.processor.addKeypadMonitor(this);
    }

    @Override
    public void parseLine(String line) {
        if (!isEnabled() || Strings.isEmpty(line) || !line.startsWith("KB,"))
            return;

        log.debug("Keypad Button Received: " + line);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            processor.sendCommand(EnableKeypadButtonMonitoring.class);
        else
            processor.sendCommand(DisableKeypadButtonMonitoring.class);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
