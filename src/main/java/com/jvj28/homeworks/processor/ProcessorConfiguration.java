package com.jvj28.homeworks.processor;

import org.springframework.beans.factory.annotation.Value;

/**
 * This configuration file is for all "HomeWorks Processor" configuration variables.
 *
 * It currently has the following configuration parameters:
 *
 *     Username:  The username of the Homeworks processor.  It's probably "LutronGUI"
 *     Password:  The password for your processor.
 *     Host:      The host name or IP address of the Lutron server.  Will use the Ethernet access ports.
 *
 * You MUST use Encrypted properties in your application.properties file.  Such as:
 *
 * hw.host=lutron.jvj28.com
 * hw.username=ENC(LLWrHy3XK+8QcLcF0jbK4XrG5EAkTtsy)
 * hw.password=ENC(RgQZi8o6Y9XEW4rGfqFyOQ==)
 *
 */
public class ProcessorConfiguration {

    @Value("${homeworks.processor.username}")
    private String username;

    @Value("${homeworks.processor.password}")
    private String password;

    @Value("${homeworks.processor.host}")
    private String host;

    @Value("${homeworks.processor.port:23}")
    private int port;

    public String getUsername() {
        return username;
    }

    public String getConsolePassword() {
        return password;
    }

    public String getConsoleHost() {
        return host;
    }

    public int getPort() { return port; }

}
