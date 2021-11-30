package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

@Data
public class StatusData implements DataObject<StatusData> {

    @Serial
    private static final long serialVersionUID = -9166231701068973133L;

    private boolean LoggedIn;
    private String processorId;
    private String osRevision;
    private String model;
    private String processorAddress;
    private String mode;
    private String processorInfo;
    private String bootRevision;

    @Override
    public StatusData generate(HomeworksProcessor processor) throws InterruptedException, ExecutionException {
        // Queue the command
        processor.sendCommand(ProcessorAddress.class)
                .onComplete(p -> {
                    this.setProcessorAddress(p.getAddress());
                    this.setMode(p.getMode());
                });
        // Queue the command
        processor.sendCommand(OSRevision.class)
                .onComplete(p -> {
                    this.setOsRevision(p.getRevision());
                    this.setProcessorId(p.getProcessorId());
                    this.setModel((p.getModel()));
                });
        // Queue the command
        processor.sendCommand(RequestBootRevisions.class)
                .onComplete(p -> {
                    this.setProcessorId(p.getProcessorId());
                    this.setBootRevision(p.getBootRevision());
                });
        // Queue the command and wait.  Makes this method synchronous
        processor.sendCommand(RequestAllProcessorStatusInformation.class)
                .onComplete(p -> this.setProcessorInfo(p.getProcessorInfo()))
                .get();
        return this;
    }
}
