package com.jvj28.homeworks.processor;

import com.jvj28.homeworks.command.HomeworksCommand;
import com.jvj28.homeworks.command.Login;
import com.jvj28.homeworks.util.Promise;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class Processor {

    private final Logger log = LoggerFactory.getLogger(Processor.class);
    private final TelnetClient telnetClient = new TelnetClient();
    private final ProcessorConfiguration config;
    // Monitors.  In the future, will replace with a List of Listener objects and a register/deregister listener process
    private final List<ProcessorMonitor> processorMonitors = new ArrayList<>();
    // Command Queue and promise. Which is more like a Future.  Maybe can replace with a standard pattern
    private final LinkedBlockingQueue<Promise<? extends HomeworksCommand>> commandQueue = new LinkedBlockingQueue<>();
    private Thread queueProcessorThread;
    private Thread dataReceiverThread;
    // Data receiver buffer
    private StringBuilder receiveBuffer = new StringBuilder();
    private Promise<? extends HomeworksCommand> currentPromise;

    // Ready Latches
    private CountDownLatch commandPromptLatch = new CountDownLatch(1);
    private CountDownLatch readyLatch = new CountDownLatch(1);
    private CountDownLatch loginPromptLatch = new CountDownLatch(1);
    private boolean stoppedOnPurpose;

    public Processor(ProcessorConfiguration config) {
        this.config = config;
    }

    void connect() throws IOException {
        if (!isConnected()) {
            log.debug("Connecting to server {} on port {}", config.getConsoleHost(), config.getPort());
            resetLatches();
            try {
                telnetClient.connect(config.getConsoleHost(), config.getPort());
            } catch (SocketException se) {
                throw new IOException(se);
            }
            try {
                startPromiseQueueProcessor();
                startDataReceiverProcessor();
            } catch (IllegalStateException ise) {
                disconnect();
                throw new IOException(ise);
            }
            log.debug("Connected");
        }
    }

    void disconnect() throws IOException {
        if (isConnected()) {
            log.debug("Disconnecting from server");
            if (dataReceiverThread != null)
                dataReceiverThread.interrupt();
            if (queueProcessorThread != null)
                queueProcessorThread.interrupt();
            telnetClient.disconnect();
            resetLatches();
        }
        log.debug("Disconnected");
    }

    private boolean waitForCommandPrompt() throws InterruptedException {
        return commandPromptLatch.await(30, TimeUnit.SECONDS);
    }

    public boolean isNotReady() throws InterruptedException {
        return !readyLatch.await(30, TimeUnit.SECONDS);
    }

    public boolean waitForLoginPrompt() throws InterruptedException {
        return loginPromptLatch.await(30, TimeUnit.SECONDS);
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

        commandQueue.clear();

        queueProcessorThread = new Thread(() -> {
            try {
                log.debug("Started Queue Processor");
                while (!queueProcessorThread.isInterrupted()) {
                    try {
                        // Wait for command prompt
                        if (waitForCommandPrompt()) { // This may timeout.  But if it does, just keep waiting
                            Promise<? extends HomeworksCommand> command = commandQueue.take();
                            if (!sendCommandToProcessor(command))
                                commandQueue.put(command);
                        }
                    } catch (InterruptedException e) {
                        log.warn("Queue processor interrupted. Stopping.");
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                log.debug("Queue Processor stopped");
                queueProcessorThread = null;
            }
        });
        queueProcessorThread.setName("Queue Runner");
        queueProcessorThread.setDaemon(true);
        queueProcessorThread.start();
    }

    private boolean sendCommandToProcessor(@NonNull Promise<? extends HomeworksCommand> promise) {

        if (!isConnected())
            return false;

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
            try {
                log.debug("Data reader started");
                while (!dataReceiverThread.isInterrupted()) {
                    try {
                        int ch = telnetClient.getInputStream().read();
                        appendToReceiveBuffer((char) ch);
                    } catch (IOException e) {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } finally {
                log.debug("Data reader stopped");
                dataReceiverThread = null;
            }
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

        for (ProcessorMonitor m : processorMonitors) {
            if (m.isEnabled())
                m.parseLine(receiveBuffer.toString());
        }

        receiveBuffer = new StringBuilder();
    }

    private void testLoginResult(Login command) {
        if (command.isSucceeded()) {
            log.debug("Login process succeeded.");
            loginPromptLatch = new CountDownLatch(1); // if we want to wait on another login prompt (such as after logout)
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
        commandQueue.add(promise);
        return promise;
    }

    public boolean queueIsNotEmpty() {
        return !commandQueue.isEmpty();
    }

    void addMonitor(ProcessorMonitor processorMonitor) {
        processorMonitors.add(processorMonitor);
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
            // since all operations are performed on a SINGLE "Queue Thread", the thread making this call is blocked.  So,
            // the expectation is that we unblock it to tell the calling thread waiting on this latch to continue.
            // See HomeworksPromiseImpl class
            currentPromise.markComplete();
            currentPromise = null;
        }
        // Can continue processing the Promise Queue.  So clear the latch.
        readyLatch.countDown(); // tell whoever is waiting that we succeeded with the login
        commandPromptLatch.countDown();
    }

    public boolean isConnected() {
        return telnetClient.isConnected();
    }

    private void resetLatches() {

        if (loginPromptLatch.getCount() == 0)
            loginPromptLatch = new CountDownLatch(1);
        if (readyLatch.getCount() == 0)
            readyLatch = new CountDownLatch(1);
        if (commandPromptLatch.getCount() == 0)
            commandPromptLatch = new CountDownLatch(1);
    }

    public void start() {
        log.info("Start Requested");
        stoppedOnPurpose = false;  // processorMonitor will attempt to login
    }

    public void stop() {
        log.info("Stop Requested");
        stoppedOnPurpose = true;  // processorMonitor will attempt to disconnect
    }

    public boolean isNotStopRequested() {
        return !stoppedOnPurpose;
    }

    public int getQueueSize() {
        return commandQueue.size();
    }
}
