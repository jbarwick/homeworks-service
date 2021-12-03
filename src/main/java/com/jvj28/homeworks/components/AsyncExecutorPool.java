package com.jvj28.homeworks.components;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class AsyncExecutorPool {

    /**
     * Get an executor bean.  This is from standard springboot EnableAsync
     * and will allow us to create a thead pool to run the HomeWorks processor commands
     * in the background.
     *
     * @return a springboot ThreadPoolTaskExecutor
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}
