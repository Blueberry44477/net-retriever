package io.github.blueberry44477.net_retriever.storage;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("server")
public class ServerStoragePathResolver implements StoragePathResolver {
    private final Path rootStorage;

    public ServerStoragePathResolver(@Value("${app.storage.root-dir}") Path rootStorage) {
        this.rootStorage = rootStorage.toAbsolutePath().normalize();
    }

    @Override
    public Path resolve(String requestedPath, String fileName) {
        Path targetDir = rootStorage;
        if (requestedPath != null && !requestedPath.isBlank()) {
            targetDir = rootStorage.resolve(requestedPath).normalize();
            if (!targetDir.startsWith(rootStorage)) {
                throw new SecurityException("Access denied: path traversal detected");
            }
        }
        return targetDir.resolve(fileName).normalize();
    }
}