package com.jvj28.homeworks;

import com.jvj28.homeworks.model.ModelConfiguration;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEncryptableProperties
public class HomeworksModel extends ModelConfiguration implements HomeworksModelMXBean {

    private final RedissonClient redis;

    public HomeworksModel(RedissonClient redis) {
        this.redis = redis;
    }

    @Override
    public String getRedisId() {
        return redis.getId();
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
        RMap<String, Object> list = redis.getMap("com.jvj28.homeworks.model.data.CircuitList");
        for (Object circuit: list.values()) {
            System.out.println(circuit.toString());
        }
    }
}
