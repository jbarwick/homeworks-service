package com.jvj28.homeworks.util;

import lombok.Data;

@Data
public class NetstatProperties {

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
