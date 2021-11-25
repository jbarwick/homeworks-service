package com.jvj28.homeworks.data.model;

import com.jvj28.homeworks.service.HomeworksProcessor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

@RedisHash("NetworkStatus")
@Data
public class Netstat implements DataObject<Netstat> {

    @Serial
    private static final long serialVersionUID = 6866420726145457381L;

    private String macAddress;
    private String ipAddress;
    private String subnetMask;
    private String gateway;
    private int telnetPort;
    private int ftpPort;
    private int httpPort;
    private boolean pingResponse;
    private String bufferHWM;
    private String socketHWM;
    private long successfulTx;
    private long errorTx;
    private long successfulRx;
    private long errorRx;

    @Override
    public Netstat generate(HomeworksProcessor processor) throws ExecutionException, InterruptedException {
            processor.sendCommand(com.jvj28.homeworks.command.Netstat.class)
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
