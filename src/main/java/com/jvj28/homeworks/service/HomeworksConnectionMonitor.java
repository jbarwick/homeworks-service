package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.RequestSystemDate;
import com.jvj28.homeworks.command.RequestSystemTime;
import com.jvj28.homeworks.data.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class HomeworksConnectionMonitor {

    private final static Logger log = LoggerFactory.getLogger(HomeworksConnectionMonitor.class);
    private static final String CONNECTION_MONITOR_THREAD = "ConnectionMonitor";
    private static final long DELAY = 10;

    private final HomeworksProcessor processor;
    private final Model model;
    private final HomeworksConfiguration config;
    private Thread thread;

    public HomeworksConnectionMonitor(HomeworksProcessor processor, Model model, HomeworksConfiguration config) {
        this.processor = processor;
        this.model = model;
        this.config = config;
    }

    @PostConstruct
    private void initialize() {
        startMonitor();
    }

    private void startMonitor() {
        this.thread = new Thread(() -> {
            try {
                // We do this loop infinitely.
                while (true) {
                    // Tell the processor object to connect

                    // Wait for the processor to become ready
                    if (!processor.waitForReady()) {
                        log.warn("Timeout waiting for processor to be ready");
                        continue;
                    }

                    // We got the command prompt, so we can begin pinning the connection
                    while (processor.isConnected()) {
                        TimeUnit.SECONDS.sleep(DELAY);
                        RequestSystemTime ptime = processor.sendCommand(RequestSystemTime.class)
                                .onComplete(p -> log.debug("Time result: {}", p.getTime())).get();
                        RequestSystemDate pdate = processor.sendCommand(RequestSystemDate.class)
                                .onComplete(p -> log.debug("Date result: {}", p.getDate())).get();
                        String dts = String.format("%s %s", pdate.getDate(), ptime.getTime());
                        log.debug("Storing processor time: {}", dts);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                        try {
                            model.setProcessorDate(sdf.parse(dts));
                        } catch (ParseException e) {
                            model.setProcessorDate(new Date());
                        }
                    }
                    processor.resetLatches();
                    // We got disconnected.  Go back and retry to log in
                }
            } catch(ExecutionException | InterruptedException e){
                log.error(e.getMessage());
            }
            log.warn("Monitor thread has exited");
        });
        this.thread.setDaemon(true);
        this.thread.setName(CONNECTION_MONITOR_THREAD);
        this.thread.start();
    }
}
