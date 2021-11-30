package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.command.RequestSystemTime;
import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

@Data
public class TimeData implements DataObject<TimeData> {

    @Serial
    private static final long serialVersionUID = -5413061624394254316L;

    private String time;

    @Override
    public TimeData generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestSystemTime.class)
                .onComplete(p -> this.setTime(p.getTime())).get();
        return this;
    }
}
