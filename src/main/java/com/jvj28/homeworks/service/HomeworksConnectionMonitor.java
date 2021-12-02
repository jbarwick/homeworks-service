package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.data.model.StatusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class HomeworksConnectionMonitor {

    private static final Logger log = LoggerFactory.getLogger(HomeworksConnectionMonitor.class);
    private static final String CONNECTION_MONITOR_THREAD = "Connection Monitor";
    private static final long DELAY = 10;

    private final Executor asyncExecutor;
    private final HomeworksProcessor processor;
    private final Model model;
    private final HomeworksConfiguration config;

    public HomeworksConnectionMonitor(@Qualifier("asyncExecutor") Executor asyncExecutor,
                                      HomeworksProcessor processor,
                                      Model model, HomeworksConfiguration config) {
        this.asyncExecutor = asyncExecutor;
        this.processor = processor;
        this.model = model;
        this.config = config;
    }

    @PostConstruct
    private void startMonitor() {
        log.debug("Starting connection monitor");
        asyncExecutor.execute(() -> {
            Thread.currentThread().setName(CONNECTION_MONITOR_THREAD);
            do {
                try {
                    attemptConnection();
                    attemptLogin();
                    attemptKeepAlive();
                } catch (InterruptedException e) {
                    log.error(e.getMessage()); // we are probably terminating
                    Thread.currentThread().interrupt();
                    return;
                }
                // We do this loop infinitely.
            } while (processor.isNotStoppedByOnPurpose());
        });
    }

    /**
     * Run a loop checking to see if the processor Telnet service is still connected.
     * If not, then let's reset the system, exit, and the loop above will attempt to reconnect
     */
    private void attemptKeepAlive() throws InterruptedException {
        while (processor.isConnected()) {
            try {
                TimeUnit.SECONDS.sleep(DELAY);
                RequestSystemTime ptime = processor.sendCommand(RequestSystemTime.class)
                        .onComplete(p -> log.debug("Time result: {}", p.getTime())).get();
                RequestSystemDate pDate = processor.sendCommand(RequestSystemDate.class)
                        .onComplete(p -> log.debug("Date result: {}", p.getDate())).get();
                String dts = String.format("%s %s", pDate.getDate(), ptime.getTime());
                log.debug("Storing processor time: {}", dts);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                model.setProcessorDate(sdf.parse(dts));
            } catch (ParseException | ExecutionException e) {
                model.setProcessorDate(new Date());
                if (e.getMessage() == null)
                    log.warn("Retrying....");
                else
                    log.warn("Retrying: {}", e.getMessage());
                processor.disconnect(); // remove this?
            }
        }
    }

    private void attemptLogin() throws InterruptedException {

        // Tell the processor object to connect
        Login loginResult = null;
        do {
            try {
                log.info("Waiting for login prompt");
                if (!processor.waitForLoginPrompt()) {
                    log.warn("Did not receive login prompt for 30 seconds");
                    continue;
                }
                log.debug("Performing login to processor");
                loginResult = processor.sendCommand(
                        new Login(config.getUsername(), config.getConsolePassword())).onComplete(p -> {
                    StatusData hw = model.get(StatusData.class, true);
                    hw.setLoggedIn(p.isSucceeded());
                    model.save(hw);
                }).get();
                if (!loginResult.isSucceeded()) {
                    log.debug("Login failed.  Retrying...");
                }
            } catch (ExecutionException e) {
                log.debug("Retrying login {}", e.getMessage());
            }
        } while ((loginResult == null) || !loginResult.isSucceeded());

        log.info("Model beginning initialization");
        processor.sendCommand(PromptOn.class);
        processor.sendCommand(ReplyOn.class);
        processor.sendCommand(ProcessorAddress.class)
                .onComplete(p -> {
                    StatusData hw = model.get(StatusData.class, true);
                    hw.setProcessorAddress(p.getAddress());
                    hw.setMode(p.getMode());
                    model.save(hw);
                });
        processor.sendCommand(OSRevision.class)
                .onComplete(p -> {
                    StatusData hw = model.get(StatusData.class, true);
                    hw.setOsRevision(p.getRevision());
                    hw.setProcessorId(p.getProcessorId());
                    hw.setModel((p.getModel()));
                    model.save(hw);
                });
        processor.sendCommand(RequestBootRevisions.class)
                .onComplete(p -> {
                    StatusData hw = model.get(StatusData.class, true);
                    hw.setProcessorId(p.getProcessorId());
                    hw.setBootRevision(p.getBootRevision());
                    model.save(hw);
                });
        processor.sendCommand(RequestAllProcessorStatusInformation.class)
                .onComplete(p -> {
                    StatusData hw = model.get(StatusData.class, true);
                    hw.setProcessorInfo(p.getProcessorInfo());
                    model.save(hw);
                });

    }

    private void attemptConnection()  {
        processor.start();
    }
}
