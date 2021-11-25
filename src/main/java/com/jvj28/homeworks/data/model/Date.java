package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;

import com.jvj28.homeworks.command.RequestSystemDate;
import org.springframework.data.redis.core.RedisHash;

import java.util.concurrent.ExecutionException;

@RedisHash("ProcessorDate")
@Data
public class Date implements DataObject<Date> {

    private static final long serialVersionUID = -784261855573465956L;

    private String date;
    private String dayOfWeek;

    @Override
    public Date generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestSystemDate.class).onComplete(p -> {
            setDate(p.getDate());
            setDayOfWeek(p.getDayOfWeek());
        }).get();
        return this;
    }
}
