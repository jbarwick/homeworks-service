package com.jvj28.homeworks.command;

import com.jvj28.homeworks.util.NetstatProperties;

public class Netstat extends NetstatProperties implements HomeworksCommand {

    @Override
    public String getName() {
        return Cmd.NETSTAT.name();
    }

    @Override
    public String getCommand() {
        return Cmd.NETSTAT.toString();
    }

    @Override
    @SuppressWarnings("java:S2692") // dude, this is MY logic.  I KNOW that '0' is a valid index.
                                    // Don't assume you know what I'm doing!
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
            setMacAddress(value);
        } else if ("IP Address".equals(label)) {
            setIpAddress(value);
        } else if ("Subnet Mask".equals(label)) {
            setSubnetMask(value);
        } else if ("Gateway".equals(label)) {
            setGateway(value);
        } else if ("Telnet Port".equals(label)) {
            setTelnetPort(Integer.parseInt(value));
        } else if ("FTP Port".equals(label)) {
            setFtpPort(Integer.parseInt(value));
        } else if ("HTTP Port".equals(label)) {
            setHttpPort(Integer.parseInt(value));
        } else if ("Ping response".equals(label)) {
            setPingResponse("ENABLED".equals(value));
        } else if ("Buffer HWM".equals(label)) {
            setBufferHWM(value);
        } else if ("Socket HWM".equals(label)) {
            setSocketHWM(value);
        } else if ("Successful Tx".equals(label)) {
            setSuccessfulTx(Long.parseLong(value));
        } else if ("Error Tx".equals(label)) {
            setErrorTx(Long.parseLong(value));
        } else if ("Successful Rx".equals(label)) {
            setSuccessfulRx(Long.parseLong(value));
        } else if ("Error Rx".equals(label)) {
            setErrorRx(Long.parseLong(value));
        }
    }

}
