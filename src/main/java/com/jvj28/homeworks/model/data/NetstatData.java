package com.jvj28.homeworks.model.data;

import com.jvj28.homeworks.command.Netstat;
import com.jvj28.homeworks.processor.HomeworksProcessor;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public class NetstatData extends NetstatProperties implements DataObject<NetstatData>, Serializable {

    private static final long serialVersionUID = 6866420726145457381L;

    @Override
    public NetstatData generate(HomeworksProcessor processor) throws ExecutionException, InterruptedException {

         processor.sendCommand(Netstat.class)
                .onComplete(p -> {
                    this.setBufferHWM(p.getBufferHWM());
                    this.setErrorRx(p.getErrorRx());
                    this.setErrorTx(p.getErrorTx());
                    this.setFtpPort(p.getFtpPort());
                    this.setGateway(p.getGateway());
                    this.setHttpPort(p.getHttpPort());
                    this.setIpAddress(p.getIpAddress());
                    this.setMacAddress(p.getMacAddress());
                    this.setPingResponse(p.isPingResponse());
                    this.setSocketHWM(p.getSocketHWM());
                    this.setSubnetMask(p.getSubnetMask());
                    this.setSuccessfulRx(p.getSuccessfulRx());
                    this.setSuccessfulTx(p.getSuccessfulTx());
                    this.setTelnetPort(p.getTelnetPort());
                }).get();
            return this;
    }
}
