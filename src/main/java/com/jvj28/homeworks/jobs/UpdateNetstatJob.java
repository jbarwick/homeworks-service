package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.data.model.Netstat;
import com.jvj28.homeworks.service.HomeworksProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        log.debug("Update Network Status");
        Netstat ns = model.get(Netstat.class, true);
        try {
            ns.generate(processor);
        } catch (ExecutionException | InterruptedException e) {
            log.warn(e.getMessage());
        }
        model.save(ns);
    }
}
