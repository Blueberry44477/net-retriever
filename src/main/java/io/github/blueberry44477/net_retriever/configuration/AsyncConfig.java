package io.github.blueberry44477.net_retriever.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService downloadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}