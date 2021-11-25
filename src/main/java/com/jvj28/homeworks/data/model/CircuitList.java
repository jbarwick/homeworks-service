package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class CircuitList implements DataObject<List<Circuit>> {

    @Override
    public List<Circuit> generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
