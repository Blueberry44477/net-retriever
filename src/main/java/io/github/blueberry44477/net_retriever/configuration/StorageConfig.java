package io.github.blueberry44477.net_retriever.configuration;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.blueberry44477.net_retriever.storage.LocalStoragePathResolver;
import io.github.blueberry44477.net_retriever.storage.ServerStoragePathResolver;
import io.github.blueberry44477.net_retriever.storage.StoragePathResolver;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
    public StoragePathResolver localStoragePathResolver() {
        return new LocalStoragePathResolver();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.mode", havingValue = "server")
    public StoragePathResolver serverStoragePathResolver(
            @Value("${app.storage.root-dir:./storage}") Path rootDir
    ) {
        return new ServerStoragePathResolver(rootDir);
    }
}