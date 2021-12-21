package com.jvj28.homeworks.model.data;

import com.jvj28.homeworks.command.RequestSystemDate;
import com.jvj28.homeworks.processor.Processor;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

@Data
public class DateData implements DataObject<DateData>, Serializable {

    private static final long serialVersionUID = -784261855573465956L;

    private String processorDate;
    private String dayOfWeek;

    @Override
    public DateData generate(Processor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestSystemDate.class).onComplete(p -> {
            setProcessorDate(p.getDate());
            setDayOfWeek(p.getDayOfWeek());
        }).get();
        return this;
    }
}
