package com.polimi.PPP.CodeKataBattle.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class TaskSchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // Example configuration
        scheduler.setThreadNamePrefix("ckb-scheduled-task-");
        scheduler.setErrorHandler(Throwable::printStackTrace); // Simple error handling
        scheduler.initialize();
        return scheduler;
    }
}
