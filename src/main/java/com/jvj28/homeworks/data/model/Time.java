package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.command.RequestSystemTime;
import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.util.concurrent.ExecutionException;

@RedisHash("ProcessorTime")
@Data
public class Time implements DataObject<Time> {

    private static final long serialVersionUID = -5413061624394254316L;

    private String time;

    @Override
    public Time generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestSystemTime.class)
                .onComplete(p -> this.setTime(p.getTime())).get();
        return this;
    }
}
