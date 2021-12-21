package com.jvj28.homeworks.model.data;

import com.jvj28.homeworks.processor.Processor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DataObject<S> {

    S generate(Processor processor) throws InterruptedException, ExecutionException, TimeoutException;

}
