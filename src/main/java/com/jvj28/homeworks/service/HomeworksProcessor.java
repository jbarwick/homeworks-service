package com.jvj28.homeworks.service;

import com.jvj28.homeworks.command.HomeworksCommand;
import com.jvj28.homeworks.command.Login;
import com.jvj28.homeworks.util.Promise;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.annotation.NonNull;

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

    private Thread queueProcessorThread;
    private Thread dataReceiverThread;

    private final TelnetClient telnetClient = new TelnetClient();
    private final HomeworksConfiguration config;

    // Monitors.  In the future, will replace with a List of Listener objects and a register/deregister listener process
    private HomeworksDimmerMonitor dimmerMonitor;
    private HomeworksKeypadButtonMonitor keypadMonitor;
    private HomeworksKeypadLEDMonitor keypadLEDMonitor;

    // Data receiver buffer
    private StringBuilder receiveBuffer = new StringBuilder();

    // Command Queue and promise. Which is more like a Future.  Maybe can replace with a standard pattern
    private final LinkedBlockingQueue<Promise<? extends HomeworksCommand>> queue = new LinkedBlockingQueue<>();
    private Promise<? extends HomeworksCommand> currentPromise;

    // Ready Latches
    private CountDownLatch commandPromptLatch = new CountDownLatch(1);
    private CountDownLatch readyLatch = new CountDownLatch(1);
    private CountDownLatch loginPromptLatch = new CountDownLatch(1);
    private boolean stoppedOnPurpose = false;

    public HomeworksProcessor(HomeworksConfiguration config) {
        this.config = config;
    }

    private boolean connect() {
        if (!isConnected()) {
            try {
                log.debug("Connecting to server {} on port {}", config.getConsoleHost(), config.getPort());
                telnetClient.connect(config.getConsoleHost(), config.getPort());
                log.debug("Connected");
                startPromiseQueueProcessor();
                startDataReceiverProcessor();

            } catch (SocketException se) {
                log.error("Cannot create TPC connection");
                return false;
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    void disconnect() {
        try {
            resetLatches();
            log.debug("Disconnecting from server");
            queue.clear();
            telnetClient.disconnect();
            log.debug("Disconnected");
        } catch (IOException e) {
            log.error("Disconnect Error: {}", e.getMessage());
        }
    }

    private boolean waitForCommandPrompt() throws InterruptedException {
        return commandPromptLatch.await(30, TimeUnit.SECONDS);
    }

    public boolean waitForReady() throws InterruptedException {
        return readyLatch.await(60, TimeUnit.SECONDS);
    }

    public boolean waitForLoginPrompt() throws InterruptedException {
        return loginPromptLatch.await(60, TimeUnit.SECONDS);
    }

    /**
     * This method does the following things:
     * 1) Uses a #CountDownLatch to wait for a command prompt before it sends the command.
     * 2) Pops a Promise off the Queue in order to run.  Was looking at embedded ActiveMQ and
     * consumers, but that seemed overkill.  So, a simple Queue list will work just fine.
     * 2) Pulls the #Command object from the Promise and sends the command to the server.  This process
     */
    private void startPromiseQueueProcessor() throws IllegalStateException {

        if (queueProcessorThread != null)
            return;

        queueProcessorThread = new Thread(() -> {
            log.debug("Started Queue Processor");
            while (isNotStoppedByOnPurpose()) {
                try {
                    // Wait for command prompt
                    if (waitForCommandPrompt()) { // This may timeout.  But if it does, just keep waiting
                        Promise<? extends HomeworksCommand> command = queue.take();
                        if (!sendCommandToProcessor(command))
                            queue.put(command);
                    }
                } catch (InterruptedException e) {
                    log.warn("Queue processor interrupted. Stopping.");
                    Thread.currentThread().interrupt();
                }
            }
            log.debug("Queue Processor stopped");
        });
        queueProcessorThread.setName("Queue Runner");
        queueProcessorThread.setDaemon(true);
        queueProcessorThread.start();
    }

    private boolean sendCommandToProcessor(@NonNull Promise<? extends HomeworksCommand> promise) {

        try {
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
            log.debug("Sending command [{}]", command.getCommand());

            OutputStream output = telnetClient.getOutputStream();
            output.write(command.getCommand().getBytes(StandardCharsets.US_ASCII));
            output.write(13); // CR
            output.write(10); // LF
            output.flush();
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    private void startDataReceiverProcessor() throws IllegalStateException {

        if (dataReceiverThread != null)
            return;

        dataReceiverThread = new Thread(() -> {
            log.debug("Data reader started");
            int ch;
            do {
                try {
                    ch = telnetClient.getInputStream().read();
                    appendToReceiveBuffer((char) ch);
                } catch (IOException e) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            } while (isNotStoppedByOnPurpose());
            log.debug("Data reader stopped");
        });
        dataReceiverThread.setName("Data Receiver");
        dataReceiverThread.setDaemon(true);
        dataReceiverThread.start();
    }

    private void appendToReceiveBuffer(char ch) {
        // if newline characters received, then add the line to the response so that it can
        // be processed by the #Command
        if (ch == 10 || ch == 13) {
            processReceivedLine();
        } else {
            receiveBuffer.append(ch);
        }

        // The space after the colon is intentional.  The full prompt is "LOGIN:<space>"
        if (receiveBuffer.indexOf("LOGIN: ", 0) >= 0) {
            receiveBuffer = new StringBuilder();
            loginPromptReceived();
            // The space after the greater-than is intentional.  The full prompt is "LNET><space>"
        } else if (receiveBuffer.indexOf("LNET> ", 0) >= 0) {
            commandPromptReceived();
        }
    }

    private void processReceivedLine() {

        if (currentPromise != null && receiveBuffer.length() > 0) {
            currentPromise.getCommand().parseLine(receiveBuffer.toString());
            if (currentPromise.getCommand() instanceof Login)
                testLoginResult((Login) currentPromise.getCommand());
        }

        if (receiveBuffer.indexOf("DL,", 0) == 0 && dimmerMonitor != null && dimmerMonitor.isEnabled())
            dimmerMonitor.parseLine(receiveBuffer.toString());
        if (receiveBuffer.indexOf("KB,", 0) == 0 && keypadMonitor != null && keypadMonitor.isEnabled())
            keypadMonitor.parseLine(receiveBuffer.toString());
        if (receiveBuffer.indexOf("KL,", 0) == 0 && keypadLEDMonitor != null && keypadLEDMonitor.isEnabled())
            keypadLEDMonitor.parseLine(receiveBuffer.toString());

        receiveBuffer = new StringBuilder();
    }

    private void testLoginResult(Login command) {
        if (command.isSucceeded()) {
            log.debug("Login process succeeded.");
            loginPromptLatch = new CountDownLatch(1); // if we want to wait on another login prompt (such as after logout)
            readyLatch.countDown(); // tell whoever is waiting that we succeeded with the login
        }
    }

    public <S extends HomeworksCommand> Promise<S> sendCommand(Class<S> clazz) {
        try {
            return sendCommand(clazz.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException ignored) {
            return null;
        }
    }

    public <S extends HomeworksCommand> Promise<S> sendCommand(S command) {
        Promise<S> promise = new PromiseImpl<>(command);
        log.debug("Adding command to queue: [{}]", command.getCommand());
        queue.add(promise);
        return promise;
    }

    public boolean queueIsNotEmpty() {
        return !queue.isEmpty();
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
    private void loginPromptReceived() {
        receiveBuffer = new StringBuilder();
        log.debug("LOGIN: prompt received");
        if (currentPromise != null) {
            // Causes the onComplete to call the Callbacks.  Tell the promise that actually, the login has failed.
            // We got another login prompt.  Which is why we are in here.  So, we failed
            currentPromise.markComplete();
            currentPromise = null;
        }
        // tell whoever is waiting that the login prompt has been received.
        loginPromptLatch.countDown();
        commandPromptLatch.countDown();
    }

    /**
     * When the command prompt is received, allow the sending thread to continue to send by
     * reducing the Latch to 0.
     */
    private void commandPromptReceived() {
        receiveBuffer = new StringBuilder();
        log.debug("LNET> prompt received");
        if (currentPromise != null) {
            // Causes the onComplete to call the Callbacks.  When we receive the LNET, this means that we have completed a command
            currentPromise.markComplete();
            currentPromise = null;
        }
        // Can continue processing the Promise Queue.  So clear the latch.
        commandPromptLatch.countDown();
    }

    public boolean isConnected() {
        return telnetClient.isConnected();
    }

    private void resetLatches() {
        loginPromptLatch = new CountDownLatch(1);
        readyLatch = new CountDownLatch(1);
        commandPromptLatch = new CountDownLatch(1);
    }

    public void start() {
        if (!isConnected()) {
            stoppedOnPurpose = false;
            connect();
        }
    }

    @PreDestroy
    public void stop() {
        if (isConnected()) {
            stoppedOnPurpose = true;
            disconnect();
        }
    }

    public boolean isNotStoppedByOnPurpose() {
        return !stoppedOnPurpose;
    }
}
