package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.util.Promise;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.annotation.NonNull;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class HomeworksProcessor {

    private final Logger log = LoggerFactory.getLogger(HomeworksProcessor.class);

    private final LinkedBlockingQueue<Promise<? extends HomeworksCommand>> queue = new LinkedBlockingQueue<>();
    private HomeworksDimmerMonitor dimmerMonitor;
    private HomeworksKeypadButtonMonitor keypadMonitor;
    private HomeworksKeypadLEDMonitor keypadLEDMonitor;
    private StringBuilder currentLine = new StringBuilder();
    private Thread queueProcessorThread;
    private Thread dataReceiverThread;
    private Promise<? extends HomeworksCommand> currentPromise;
    private CountDownLatch commandPromptLatch = new CountDownLatch(1);
    private CountDownLatch processorReadyLatch = new CountDownLatch(1);
    private final TelnetClient telnetClient = new TelnetClient();
    private final HomeworksConfiguration config;

    public HomeworksProcessor(HomeworksConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void connect() {
        if (telnetClient.isConnected()) {
            log.debug("Already connected to server");
            return;
        }
        try {
            processorReadyLatch = new CountDownLatch(1);
            log.debug("Connecting to server " + config.getConsoleHost() + " on port " + config.getPort());
            telnetClient.connect(config.getConsoleHost(), config.getPort());
            log.debug("Connected");
            startPromiseQueueProcessor();
            startDataReceiverProcessor();
        } catch (SocketException se) {
            log.error("Cannot create TPC connection");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            processorReadyLatch = new CountDownLatch(1);
            log.debug("Disconnecting from server");
            queue.clear();
            telnetClient.disconnect();
            log.debug("Disconnected");
        } catch (IOException e) {
            log.error("Disconnect Error " + e.getMessage());
        }
    }

    /**
     * This method does the following things:
     * 1) Uses a #CountDownLatch to wait for a command prompt before it sends the command.
     * 2) Pops a Promise of of the Queue in order to run.  Was looking at embedded ActiveMQ and
     * consumers, but that seemed overkill.  So, a simple Queue list will work just fine.
     * 2) Pulls the #Command object from the Promise and sends the command to the server.  This process
     */
    private void startPromiseQueueProcessor() throws Exception {

        if (queueProcessorThread != null) {
            throw new Exception("Queue processor already started");
        }

        queueProcessorThread = new Thread(() -> {
            log.debug("Started Queue Processor");
            try {
                while (telnetClient.isConnected()) {
                    // Wait for command prompt
                    if (commandPromptLatch.await(30, TimeUnit.SECONDS)) {
                        Promise<? extends HomeworksCommand> command = queue.take();
                        try {
                            sendCommandToProcessor(command);
                        } catch (Exception e) {
                            queue.put(command);
                            log.warn(e.getMessage());
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.warn("Queue processor interrupted. Stopping.");
            } finally {
                try {
                    telnetClient.disconnect(); // make sure.  No error if already disconnected
                } catch (Exception ignored) { }
                queueProcessorThread = null;
            }
            log.debug("Queue Processor stopped");
        });
        queueProcessorThread.setName("Queue Runner");
        queueProcessorThread.setDaemon(true);
        queueProcessorThread.start();
    }

    private void sendCommandToProcessor(@NonNull Promise<? extends HomeworksCommand> promise) throws IOException {

        // Reset the latch because we cannot send another command until a command prompt has been received
        // Note that when the command prompt is received, the current Promise in operation will be null.
        // If this operation throws an IO Exception, we will assume we are no longer connected to the
        // processor and the command countdown latch will remain 1 until reconnected and a prompt is received.
        commandPromptLatch = new CountDownLatch(1);

        // currentPromise should be set to null before the countdown latch is decremented.  This happens ONLY
        // when a command prompt is received.
        assert currentPromise == null;

        // We will save this for the reader and append received content to this active Promise.
        // "There can be only one" active Promise.
        currentPromise = promise;

        HomeworksCommand command = currentPromise.getCommand();
        log.debug("Sending command [" + command.getCommand() + "]");

        OutputStream output = telnetClient.getOutputStream();
        output.write(command.getCommand().getBytes(StandardCharsets.US_ASCII));
        output.write(13);
        output.write(10);
        output.flush();
    }

    private void startDataReceiverProcessor() throws Exception {

        if (dataReceiverThread != null) {
            throw new Exception("Data receiver already started");
        }

        dataReceiverThread = new Thread(() -> {
            log.debug("Data reader started");
            int ch;
            do {
                try {
                    ch = telnetClient.getInputStream().read();
                    write((char) ch);
                } catch (IOException e) {
                    ch = -1;
                }
            } while (ch >= 0 && telnetClient.isConnected());
            log.debug("Data reader stopped");
        });
        dataReceiverThread.setName("Data Receiver");
        dataReceiverThread.setDaemon(true);
        dataReceiverThread.start();
    }

    private void write(char ch) {
        // if newline characters received, then add the line to the response so that it can
        // be processed by the #Command
        if (ch == 10 || ch == 13) {
            if (currentPromise != null && currentLine.length() > 0) {
                currentPromise.getCommand().parseLine(currentLine.toString());
            }
            if (currentLine.indexOf("DL,", 0) == 0) {
                if (dimmerMonitor != null && dimmerMonitor.isEnabled())
                    dimmerMonitor.parseLine(currentLine.toString());
            }
            if (currentLine.indexOf( "KB,", 0) == 0) {
                if (keypadMonitor !=null && keypadMonitor.isEnabled())
                    keypadMonitor.parseLine(currentLine.toString());
            }
            if (currentLine.indexOf( "KL,", 0) == 0) {
                if (keypadLEDMonitor !=null && keypadLEDMonitor.isEnabled())
                    keypadLEDMonitor.parseLine(currentLine.toString());
            }
            currentLine = new StringBuilder();
        } else {
            currentLine.append(ch);
        }

        // The space after the colon is intentional.  The full prompt is "LOGIN:<space>"
        if (currentLine.indexOf("LOGIN: ", 0) >= 0) {
            currentLine = new StringBuilder();
            LoginPromptReceived();
            // The space after the greater-than is intentional.  The full prompt is "LNET><space>"
        } else if (currentLine.indexOf("LNET> ", 0) >= 0) {
            currentLine = new StringBuilder();
            CommandPromptReceived();
        }
    }

    public <S extends HomeworksCommand> Promise<S> sendCommand(Class<S> clazz) {
        try {
            return sendCommand(clazz.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException ignored) {
        }
        return null;
    }

    public <S extends HomeworksCommand> Promise<S> sendCommand(S command) {
        Promise<S> promise = new PromiseImpl<>(command);
        log.debug("Adding command to queue: [" + command.getCommand() + "]");
        queue.add(promise);
        return promise;
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public int getQueueDepth() {
        return queue.size();
    }

    void addDimmerMonitor(HomeworksDimmerMonitor monitor) {
        dimmerMonitor = monitor;
    }

    void addKeypadMonitor(HomeworksKeypadButtonMonitor monitor) {
        keypadMonitor = monitor;
    }

    void addKeypadLEDMonitor(HomeworksKeypadLEDMonitor monitor) {
        keypadLEDMonitor = monitor;
    }

    /**
     * When the login prompt is received, allow the sending thread to send content to the coprocessor
     * by reducing the Latch to 0.
     */
    private void LoginPromptReceived() {
        log.debug("LOGIN: prompt received");
        if (currentPromise != null) {
            // Causes the onComplete to call the Callbacks
            currentPromise.markComplete();
            currentPromise = null;
        }
        // We can continue processing the Promise Queue.  So clear the latch.
        commandPromptLatch.countDown();
        processorReadyLatch.countDown();
    }

    /**
     * When the command prompt is received, allow the sending thread to continue to send by
     * reducing the Latch to 0.
     */
    private void CommandPromptReceived() {
        if (currentPromise != null) {
            // Causes the onComplete to call the Callbacks.
            currentPromise.markComplete();
            currentPromise = null;
        }
        log.debug("LNET> prompt received");
        // Can continue processing the Promise Queue.  So clear the latch.
        commandPromptLatch.countDown();
    }

}
