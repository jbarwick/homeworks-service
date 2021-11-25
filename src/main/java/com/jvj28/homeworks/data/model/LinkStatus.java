package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.command.RequestLinkShortStatus;
import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;

import java.util.concurrent.ExecutionException;

@Data
public class LinkStatus implements DataObject<LinkStatus> {

    private static final long serialVersionUID = -657472216331498686L;

    private String processorId;
    private String linkStatus;

    @Override
    public LinkStatus generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        processor.sendCommand(RequestLinkShortStatus.class)
                .onComplete(p -> {
                    this.setLinkStatus(p.getLinkStatus());
                    this.setProcessorId(p.getProcessorId());
                }).get();
        return this;
    }
}
