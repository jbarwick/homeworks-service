package com.jvj28.homeworks.processor;

import com.jvj28.homeworks.command.HomeworksCommand;
import com.jvj28.homeworks.util.Promise;
import com.jvj28.homeworks.util.PromiseCallback;
import org.springframework.data.annotation.Immutable;
import org.springframework.lang.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Immutable
class PromiseImpl<T extends HomeworksCommand> implements Promise<T> {

    // Only allow a promise to run for 30 seconds.  Else, nothing will happen.  The callback will not execute.
    private static final int TIMEOUT = 30;
    // This is the command that is sent to the processor.  It is passed to the callback function so that you
    // can do something with it.  The HomeworksProcessor will call the parseLine and then update the processorLatch
    private final T command;
    // Used to tell other threads that all callback functions have completed
    private CountDownLatch callbackLatch = new CountDownLatch(0);
    // Used to tell this thread the process has finished the command
    private final CountDownLatch processorLatch = new CountDownLatch(1);

    private Thread promiseThread;

    private boolean cancelled;

    public PromiseImpl(T command) {
        this.command = command;
    }

    public T getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command.toString();
    }

    @Override
    public Promise<T> onComplete(PromiseCallback<T> callback) {
        callbackLatch = new CountDownLatch(1);
        promiseThread = new Thread(() -> {
            try {
                if (this.processorLatch.await(TIMEOUT, TimeUnit.SECONDS) && !this.cancelled) {
                    callback.onComplete(command);
                }
            }
            catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            finally {
                promiseThread = null;
                callbackLatch.countDown();
            }
        });
        promiseThread.setName(command.getClass().getSimpleName());
        promiseThread.setDaemon(true);
        promiseThread.start();
        return this;
    }

    @Override
    public void markComplete() {
        this.processorLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (promiseThread == null)
            return false;
        this.cancelled = true;
        if (mayInterruptIfRunning) {
            promiseThread.interrupt();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return callbackLatch.getCount() == 0L;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
           throw new ExecutionException("Execution Failed", e);
        }
    }

    @Override
    public T get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!this.callbackLatch.await(timeout, unit))
            throw new TimeoutException("No response after time limit");
        return command;
    }
}
