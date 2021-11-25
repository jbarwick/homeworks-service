package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DataObject<S> extends Serializable {

    S generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException, TimeoutException;

}
