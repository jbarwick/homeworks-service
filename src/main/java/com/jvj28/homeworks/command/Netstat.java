package com.jvj28.homeworks.command;

import lombok.Data;

@Data
public class Netstat implements HomeworksCommand {

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
    public String getName() {
        return Cmd.NETSTAT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.NETSTAT.toString();
    }

    @Override
    public void parseLine(String line) {
        if (line == null || line.length() == 0)
            return;
        String[] parts = null;
        if (line.indexOf("=") > 0) {
            parts = line.split("=");
        } else if (line.indexOf(":") > 0) {
            parts = line.split(":");
        }
        if (parts != null && parts.length == 2)
            setField(parts[0].trim(),parts[1].trim());
    }

    private void setField(String label, String value) {
        if ("MAC Address".equals(label)) {
            this.macAddress = value;
        } else if ("IP Address".equals(label)) {
            this.ipAddress = value;
        } else if ("Subnet Mask".equals(label)) {
            this.subnetMask = value;
        } else if ("Gateway".equals(label)) {
            this.gateway = value;
        } else if ("Telnet Port".equals(label)) {
            this.telnetPort = Integer.parseInt(value);
        } else if ("FTP Port".equals(label)) {
            this.ftpPort = Integer.parseInt(value);
        } else if ("HTTP Port".equals(label)) {
            this.httpPort = Integer.parseInt(value);
        } else if ("Ping response".equals(label)) {
            this.pingResponse = "ENABLED".equals(value);
        } else if ("Buffer HWM".equals(label)) {
            this.bufferHWM = value;
        } else if ("Socket HWM".equals(label)) {
            this.socketHWM = value;
        } else if ("Successful Tx".equals(label)) {
            this.successfulTx = Long.parseLong(value);
        } else if ("Error Tx".equals(label)) {
            this.errorTx = Long.parseLong(value);
        } else if ("Successful Rx".equals(label)) {
            this.successfulRx = Long.parseLong(value);
        } else if ("Error Rx".equals(label)) {
            this.errorRx = Long.parseLong(value);
        }
    }

}
