package com.jvj28.homeworks;

public interface HomeworksModelMXBean {

    String getCircuitsSeedFilename();

    String getKeypadSeedFilename();

    String getUsersSeedFilename();

    String getRedisId();

    String getRedisHost();

    int getRedisPort();

    void printRedisKeys();

    void flushRedisKeys();

    void printCircuits();
}
