package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class KeypadList implements DataObject<List<Keypad>> {
    @Override
    public List<Keypad> generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
