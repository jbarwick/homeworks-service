package com.jvj28.homeworks.util;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class NetstatProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8145361970543775115L;

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
}
