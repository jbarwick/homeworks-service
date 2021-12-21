package com.jvj28.homeworks;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.ModelConfiguration;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HomeworksModel implements HomeworksModelMXBean {

    private final ModelConfiguration config;
    private final Model model;
    private final RedissonClient redis;

    public HomeworksModel(ModelConfiguration config, Model model, RedissonClient redis) {
        this.config = config;
        this.model = model;
        this.redis = redis;
    }

    @Override
    public String getCircuitsSeedFilename() {
        return config.getCircuitsSeedFilename();
    }

    @Override
    public String getKeypadSeedFilename() {
        return config.getKeypadSeedFilename();
    }

    @Override
    public String getUsersSeedFilename() {
        return config.getUsersSeedFilename();
    }

    @Override
    public String getRedisId() {
        return redis.getId();
    }

    @Override
    public String getRedisHost() {
        return config.getRedisHost();
    }

    @Override
    public int getRedisPort() {
        return config.getRedisPort();
    }

    @Override
    public void printRedisKeys() {
        RKeys keys = redis.getKeys();
        long total = keys.count();
        long count = 1;
        for (String key: keys.getKeys()) {
            System.out.printf("REDIS Key %d/%d: %s%n", count, total, key);
            count = count + 1;
        }
    }

    @Override
    public void flushRedisKeys() {
        RKeys keys = redis.getKeys();
        System.out.printf("Flush %d Redis keys%n", keys.count());
        keys.flushall();
    }

    @Override
    public void printCircuits() {
        List<CircuitEntity> circuits = model.getCircuits();
        for (CircuitEntity circuit: circuits) {
            System.out.println(circuit);
        }
    }
}
