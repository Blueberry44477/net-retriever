package io.github.blueberry44477.net_retriever.service.storage;

import java.nio.file.Path;

/**
 * StoragePathResolver
 */
public interface StoragePathResolver {
    Path resolve(String requestedPath, String fileName);
}