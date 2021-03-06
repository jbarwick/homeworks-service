package com.jvj28.homeworks.processor;

import com.jvj28.homeworks.command.DisableDimmerLevelMonitoring;
import com.jvj28.homeworks.command.EnableDimmerLevelMonitoring;
import com.jvj28.homeworks.command.RequestZoneLevel;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Initialized by the HomeworksCacheService and passed to the CommandProcessor.
 * The idea is to update the Cache when the Processor transmits a change in Dimmer Levels.
 * The Quartz Job InitializeDimmerValuesJob enables or disabled this monitor.
 */
@Component
public class DimmerProcessorMonitor implements ProcessorMonitor {

    private static final Logger log = LoggerFactory.getLogger(DimmerProcessorMonitor.class);

    private final Model model;
    private final Processor processor;

    private boolean enabled = false;

    public DimmerProcessorMonitor(Processor processor, Model model) {
        this.model = model;
        this.processor = processor;
        this.processor.addMonitor(this);
    }

    // Used by CommandProcessor when it receives a monitor.
    @Override
    public void parseLine(String line) {
        if (!isEnabled() || Strings.isEmpty(line) || !line.startsWith("DL,"))
            return;

        log.debug("New Dimmer value received: {}", line);

        RequestZoneLevel zoneLevel = new RequestZoneLevel();
        zoneLevel.parseLine(line);

        String address = zoneLevel.getAddress();
        int level = zoneLevel.getLevel();

        // Get the circuit from the model and update it's level
        CircuitEntity circuit = model.findCircuitByAddress(address);
        if (circuit != null) {
            circuit.setLevel(level);
            model.saveCircuit(circuit);
            log.debug("Circuit [{}] at {}%", address, level);
        } else {
            log.warn("Cannot find circuit [{}] while attempting to update levels", address);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            processor.sendCommand(EnableDimmerLevelMonitoring.class);
        else
            processor.sendCommand(DisableDimmerLevelMonitoring.class);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
