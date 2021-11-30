package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.command.Netstat;
import com.jvj28.homeworks.service.HomeworksProcessor;
import com.jvj28.homeworks.util.NetstatProperties;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

public class NetstatData extends NetstatProperties implements DataObject<NetstatData> {

    @Serial
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
