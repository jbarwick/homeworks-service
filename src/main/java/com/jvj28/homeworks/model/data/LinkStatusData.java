package com.jvj28.homeworks.model.data;

import com.jvj28.homeworks.command.RequestLinkShortStatus;
import com.jvj28.homeworks.processor.HomeworksProcessor;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

@Data
public class LinkStatusData implements DataObject<LinkStatusData>, Serializable {

    private static final long serialVersionUID = -657472216331498686L;

    private String processorId;
    private String linkStatus;

    @Override
    public LinkStatusData generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestLinkShortStatus.class)
                .onComplete(p -> {
                    this.setLinkStatus(p.getLinkStatus());
                    this.setProcessorId(p.getProcessorId());
                }).get();
        return this;
    }
}
