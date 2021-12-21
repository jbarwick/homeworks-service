package com.jvj28.homeworks.model;

import org.springframework.beans.factory.annotation.Value;

public class ModelConfiguration {

    @Value("${hw.circuits.seed:circuit_zones.csv}")
    private String seedCircuits;

    @Value("${hw.keypads.seed:keypads.csv}")
    private String seedKeypads;

    @Value("${hw.users.seed:users.csv}")
    private String seedUsers;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    public String getCircuitsSeedFilename() {
        return seedCircuits;
    }

    public String getKeypadSeedFilename() {
        return seedKeypads;
    }

    public String getUsersSeedFilename() {
        return seedUsers;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

}
