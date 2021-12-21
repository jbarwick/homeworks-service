package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.command.RequestAllProcessorStatusInformation;
import com.jvj28.homeworks.processor.Processor;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class UpdateStatusJob extends QuartzJobBean {

    private final Logger log = LoggerFactory.getLogger(UpdateStatusJob.class);

    private final Processor processor;

    public UpdateStatusJob(Processor processor) {
        this.processor = processor;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        Thread.currentThread().setName("Refresh Status");

        this.processor.sendCommand(RequestAllProcessorStatusInformation.class).onComplete(status ->
                log.debug("Processor info: {}",status.getProcessorInfo())
        );
    }
}
