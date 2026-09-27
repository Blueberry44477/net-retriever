package io.github.blueberry44477.net_retriever.storage;

import java.nio.file.Path;

public interface StoragePathResolver {
    Path resolve(String requestedPath, String fileName);
}