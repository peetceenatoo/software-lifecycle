package com.polimi.PPP.CodeKataBattle.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Sets the core number of threads
        executor.setMaxPoolSize(5); // Sets the maximum allowed number of threads
        executor.setQueueCapacity(500); // Sets the capacity of the queue
        executor.setThreadNamePrefix("AsyncEvaluator-");
        executor.initialize();
        return executor;
    }
}

