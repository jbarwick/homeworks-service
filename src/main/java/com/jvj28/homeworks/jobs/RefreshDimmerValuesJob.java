package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.command.RequestZoneLevel;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import com.jvj28.homeworks.processor.DimmerProcessorMonitor;
import com.jvj28.homeworks.processor.Processor;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class RefreshDimmerValuesJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(RefreshDimmerValuesJob.class);

    private final Processor processor;
    private final Model model;
    private final DimmerProcessorMonitor dimmerMonitor;

    public RefreshDimmerValuesJob(Processor processor,
                                  Model model,
                                  DimmerProcessorMonitor dimmerMonitor) {
        this.processor = processor;
        this.model = model;
        this.dimmerMonitor = dimmerMonitor;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {
        try {
            Thread.currentThread().setName("Refresh Dimmers");

            //noinspection StatementWithEmptyBody
            while (processor.isNotReady());

            log.info("Dimmer refresh job execute");
            dimmerMonitor.setEnabled(false);
            model.getCircuits().forEach(circuit -> processor.sendCommand(
                    new RequestZoneLevel(circuit.getAddress())).onComplete(request -> {
                        CircuitEntity c = model.findCircuitByAddress(request.getAddress());
                        if (c != null) {
                            c.setLevel(request.getLevel());
                            model.saveCircuit(c);
                            log.debug("Circuit [{}] at {}%", request.getAddress(), request.getLevel());
                        }
                    }
            ));
            // This is not synchronous.  It simply adds the command to the command queue
            dimmerMonitor.setEnabled(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
