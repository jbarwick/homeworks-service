package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.UsageByMinuteEntity;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SaveDimmerValuesJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(SaveDimmerValuesJob.class);

    private final Model model;

    public SaveDimmerValuesJob(Model cache) {
        this.model = cache;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {
        Thread.currentThread().setName("Save Usage");
        log.debug("Save Watts to Db Job Started");
        int watts = model.getCurrentUsage();
        UsageByMinuteEntity usage = new UsageByMinuteEntity(Instant.now());
        usage.setWatts(watts);
        log.debug("Saving {}", usage);
        model.saveUsage(usage);
    }
}
