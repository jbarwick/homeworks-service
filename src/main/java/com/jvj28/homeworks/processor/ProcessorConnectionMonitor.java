package com.jvj28.homeworks.processor;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.StatusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class ProcessorConnectionMonitor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorConnectionMonitor.class);
    private static final String CONNECTION_MONITOR_THREAD = "Connection Monitor";
    private static final long DELAY = 10;

    private final Executor asyncExecutor;
    private final Processor processor;
    private final Model model;
    private final ProcessorConfiguration config;

    public ProcessorConnectionMonitor(@Qualifier("asyncExecutor") Executor asyncExecutor,
                                      Processor processor,
                                      Model model, ProcessorConfiguration config) {
        this.asyncExecutor = asyncExecutor;
        this.processor = processor;
        this.model = model;
        this.config = config;
    }

    @PostConstruct
    private void startMonitor() {
        asyncExecutor.execute(() -> {
            Thread.currentThread().setName(CONNECTION_MONITOR_THREAD);
            try {
                runConnectionMonitorSteps();
            } catch (InterruptedException e) {
                log.warn("Monitor Thread Interrupted");
                processor.stop();
                Thread.currentThread().interrupt();
            }
        });
    }

    private void runConnectionMonitorSteps() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            log.debug("Starting connection monitor");
            if (processor.isNotStopRequested()) {
                try {
                    attemptConnection();
                    attemptLogin();
                    attemptKeepAlive();
                } catch (IOException e) {
                    log.error("Monitor Exception: {}", e.getMessage());
                } finally {
                    attemptDisconnect();
                }
            } else {
                log.warn("Processor is stopped on purpose.  Waiting.");
            }
            TimeUnit.SECONDS.sleep(5);
        }
    }

    /**
     * Run a loop checking to see if the processor Telnet service is still connected.
     * If not, then let's reset the system, exit, and the loop above will attempt to reconnect
     */
    private void attemptKeepAlive() throws InterruptedException, IOException {
        while (processor.isNotStopRequested()) {

            log.info("Keep-Alive ping...");

            if (!processor.isConnected())
                throw new IOException("Keep-Alive process exiting due to disconnect");

            try {

                // This is the current "ping" process to keep the connection alive.
                // This could be replaced by simply sending a CRLF to the processor. But
                // I think getting the system time is cool.  And we pass it to Pegasus

                RequestSystemTime ptime = processor.sendCommand(RequestSystemTime.class)
                        .onComplete(p -> log.debug("Time result: {}", p.getTime())).get();
                RequestSystemDate pDate = processor.sendCommand(RequestSystemDate.class)
                        .onComplete(p -> log.debug("Date result: {}", p.getDate())).get();
                String dts = String.format("%s %s", pDate.getDate(), ptime.getTime());
                log.debug("Storing processor time: {}", dts);

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                model.setProcessorDate(sdf.parse(dts));

            } catch (ParseException pe) {
                log.error(pe.getMessage());
            } catch (ExecutionException e) {
                throw new IOException(e);
            }
            TimeUnit.SECONDS.sleep(DELAY);
        }
        log.warn("Keep-Alive has stopped");
    }

    private void attemptLogin() throws InterruptedException, IOException {

        log.info("Login to processor");

        // Tell the processor object to connect
        Login loginResult;

        if (!processor.isConnected())
            throw new IOException("Login process exiting due to disconnect");

        if (!processor.waitForLoginPrompt())
            throw new IOException("Did not receive login prompt for 30 seconds");

        try {
            log.debug("Performing login to processor");
            loginResult = processor.sendCommand(
                    new Login(config.getUsername(), config.getConsolePassword())).onComplete(p -> {
                StatusData hw = model.get(StatusData.class, true);
                hw.setLoggedIn(p.isSucceeded());
                model.save(hw);
            }).get();
        } catch (ExecutionException e) {
            throw new IOException(e.getMessage());
        }

        if (loginResult == null || !loginResult.isSucceeded())
            throw new IOException("Login failed.  Retrying...");

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

    private void attemptConnection() throws IOException {
        log.info("Connecting to and Starting processor");
        processor.connect();
    }

    private void attemptDisconnect() {
        log.info("Close/Reset Connections");
        try {
            processor.disconnect();
        } catch (IOException e) {
            log.warn("Error during disconnect: {}", e.getMessage());
        }
    }
}