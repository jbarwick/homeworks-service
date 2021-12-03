package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.service.HomeworksProcessor;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class UpdateNetstatJob extends QuartzJobBean {

    private final Logger log = LoggerFactory.getLogger(UpdateNetstatJob.class);

    private final HomeworksProcessor processor;
    private final Model model;

    public UpdateNetstatJob(HomeworksProcessor processor, Model model) {
        this.processor = processor;
        this.model = model;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        Thread.currentThread().setName("Refresh Netstat");
        log.debug("Scheduler Update Network Status (with forUpdate)");
        NetstatData ns = model.get(NetstatData.class, true);
        try {
            processor.waitForReady();
            log.debug("Generating new Netstat Data");
            ns.generate(processor);
            model.save(ns); // Save and release the lock
        } catch (ExecutionException ee) {
            log.warn("Could not generate data object: {}", ee.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
