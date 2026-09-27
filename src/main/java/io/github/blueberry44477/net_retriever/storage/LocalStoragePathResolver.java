package io.github.blueberry44477.net_retriever.storage;

import java.nio.file.Path;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalStoragePathResolver implements StoragePathResolver {

    private final Path defaultDirectory = Path.of(System.getProperty("user.home"), "Downloads");

    @Override
    public Path resolve(String requestedPath, String fileName) {
        Path baseDir = (requestedPath != null && !requestedPath.isBlank())
                ? Path.of(requestedPath)
                : defaultDirectory;

        return baseDir.resolve(fileName).toAbsolutePath().normalize();
    }
}