package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.service.HomeworksConfiguration;
import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.service.HomeworksProcessor;
import org.quartz.JobExecutionContext;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class UpdateProcessorStatus extends QuartzJobBean {

    private final HomeworksProcessor processor;
    private final HomeworksConfiguration config;
    private final Model data;

    public UpdateProcessorStatus(HomeworksProcessor processor, HomeworksConfiguration config, Model data) {
        this.processor = processor;
        this.config = config;
        this.data = data;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {

    }
}
