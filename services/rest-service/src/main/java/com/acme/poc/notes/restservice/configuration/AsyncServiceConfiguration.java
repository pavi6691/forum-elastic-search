package com.acme.poc.notes.restservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncServiceConfiguration {

    @Value("${service.thread.pool.size}")
    private int service_thread_pool_size;
    @Bean(name = "asyncExecutor")
    public Executor asyncServiceExecutor()  {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(service_thread_pool_size);
        executor.setMaxPoolSize(service_thread_pool_size);
        executor.setThreadNamePrefix("AsyncServiceConfiguration-");
        executor.initialize();
        return executor;
    }
}