package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.command.RequestZoneLevel;
import com.jvj28.homeworks.data.model.Circuit;
import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.service.HomeworksDimmerMonitor;
import com.jvj28.homeworks.service.HomeworksProcessor;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RefreshDimmerValuesJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(RefreshDimmerValuesJob.class);

    private final HomeworksProcessor processor;
    private final Model model;
    private final HomeworksDimmerMonitor dimmerMonitor;

    public RefreshDimmerValuesJob(HomeworksProcessor processor,
                                  Model model,
                                  HomeworksDimmerMonitor dimmerMonitor) {
        this.processor = processor;
        this.model = model;
        this.dimmerMonitor = dimmerMonitor;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {

        log.debug("Dimmer refresh job execute");

        dimmerMonitor.setEnabled(false);
        model.getCircuits().forEach(circuit -> {
            processor.sendCommand(new RequestZoneLevel(circuit.getAddress())).onComplete(
                    request -> {
                        Circuit c = model.findCircuitByAddress(request.getAddress());
                        if (c!= null) {
                            c.setLevel(request.getLevel());
                            model.saveCircuit(c);
                            log.debug(String.format("Circuit [%s] at %d%%", request.getAddress(), request.getLevel()));
                        }
                    }
            );
        });
        // This is not synchronous.  It simply adds the command to the command queue
        dimmerMonitor.setEnabled(true);
    }
}
