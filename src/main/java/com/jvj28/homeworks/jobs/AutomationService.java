package com.jvj28.homeworks.jobs;

import com.jvj28.homeworks.service.HomeworksProcessor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
public class AutomationService {

    private static final Logger log = LoggerFactory.getLogger(AutomationService.class);

    private final Executor asyncExecutor;
    private final HomeworksProcessor processor;
    private final Scheduler scheduler;

    public AutomationService(@Qualifier("asyncExecutor") Executor asyncExecutor,
                             HomeworksProcessor processor, @Qualifier("quartzScheduler") Scheduler scheduler) {
        this.asyncExecutor = asyncExecutor;
        this.processor = processor;
        this.scheduler = scheduler;
    }

    @PostConstruct
    private void onSystemBooted() {

        asyncExecutor.execute(() -> {
            try {
                Thread.currentThread().setName("Job Initializer");
                processor.waitForReady();

                log.debug("Enabling Processor Status Refresh Job (Refresh every hour)");
                JobDetail statusUpdate = getJobDetail(UpdateStatusJob.class);
                scheduler.scheduleJob(statusUpdate, buildJobTrigger(statusUpdate, 60, 10));

                log.debug("Enabling Dimmer Values Refresh Job (Refresh every hour to ensure synced to Level Monitor)");
                JobDetail dimmerInitializer = getJobDetail(RefreshDimmerValuesJob.class);
                scheduler.scheduleJob(dimmerInitializer, buildJobTrigger(dimmerInitializer, 60, 10));

                log.debug("Enabling Logging of Dimmer Values to Database.  Logged every 60 seconds.");
                JobDetail dimmerRecorder = getJobDetail(SaveDimmerValuesJob.class);
                scheduler.scheduleJob(dimmerRecorder, buildJobTrigger(dimmerRecorder, 1, 20));

                log.debug("Enabling Network Status Refresh.  Updates counters every 60 seconds.");
                JobDetail netstatUpdater = getJobDetail(UpdateNetstatJob.class);
                scheduler.scheduleJob(netstatUpdater, buildJobTrigger(netstatUpdater, 1, 21));

            } catch (SchedulerException se) {
                log.error("Something happened to the scheduler: {}", se.getMessage());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    private JobDetail getJobDetail(Class<? extends QuartzJobBean> clazz) {
        String name = clazz.getName();
        log.debug("Scheduling Job: {}", name);
        return JobBuilder.newJob(clazz)
                .withIdentity(UUID.randomUUID().toString(), "hw-jobs")
                .withDescription(name)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, int repeatEveryMinutes, int delaySeconds) {
        Instant instant = ZonedDateTime.now().plusSeconds(delaySeconds).toInstant();
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "hw-triggers")
                .withDescription(jobDetail.getDescription())
                .startAt(Date.from(instant))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(repeatEveryMinutes)
                        .repeatForever())
                .build();
    }

}
