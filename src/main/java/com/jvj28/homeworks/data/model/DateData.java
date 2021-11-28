package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;

import com.jvj28.homeworks.command.RequestSystemDate;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

@Data
public class DateData implements DataObject<DateData> {

    @Serial
    private static final long serialVersionUID = -784261855573465956L;

    private String dateData;
    private String dayOfWeek;

    @Override
    public DateData generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestSystemDate.class).onComplete(p -> {
            setDateData(p.getDate());
            setDayOfWeek(p.getDayOfWeek());
        }).get();
        return this;
    }
}