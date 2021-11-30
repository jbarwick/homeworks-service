package com.jvj28.homeworks.service;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

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
@Configuration
@EnableAsync
@EnableEncryptableProperties
public class HomeworksConfiguration {

    @Value("${hw.username}")
    private String username;

    @Value("${hw.password}")
    private String password;

    @Value("${hw.host}")
    private String host;

    @Value("${hw.port:23}")
    private int port;

    @Value("${hw.circuits.seed:circuit_zones.csv}")
    private String seedCircuits;

    @Value("${hw.keypads.seed:keypads.csv}")
    private String seedKeypads;

    @Value("${hw.users.seed:users.csv}")
    private String seedUsers;

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

    public String getCircuitsSeedFilename() {
        return seedCircuits;
    }

    public String getKeypadSeedFilename() {
        return seedKeypads;
    }

    public String getUsersSeedFilename() {
        return seedUsers;
    }
}
