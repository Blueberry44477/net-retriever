package io.github.blueberry44477.net_retriever.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.blueberry44477.net_retriever.model.DownloadTask;
import io.github.blueberry44477.net_retriever.repository.DownloadTaskRepository;
import io.github.blueberry44477.net_retriever.service.downloader.DownloadMetadata;
import io.github.blueberry44477.net_retriever.service.downloader.FileDownloader;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DownloadTaskService {
    private final DownloadTaskRepository repository;
    private final List<FileDownloader> downloaders;
    private final ExecutorService executorService;
    private final Map<UUID, Future<?>> activeTasks = new ConcurrentHashMap<>();

    @Transactional
    public DownloadTask createTask(URI source, Path destinationDirectory) throws IOException {
        FileDownloader downloader = downloaders.stream()
            .filter(d -> d.supports(source))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported protocol: " + source.getScheme()));

        DownloadMetadata metadata = downloader.fetchMetadata(source);

        String fileName = resolveFileName(source, metadata);
        Path fullDestinationPath = destinationDirectory.resolve(fileName);

        return new DownloadTask(source.toString(), fileName, fullDestinationPath.toString());
    }

    private String resolveFileName(URI source, DownloadMetadata metadata) {
        if (metadata.getSuggestedFileName() != null && !metadata.getSuggestedFileName().isBlank()) {
            return metadata.getSuggestedFileName();
        }
        
        // Get last index of URI if server responses with no Content-Disposition.
        String path = source.getPath();
        if (path != null && path.contains("/")) {
            String extracted = path.substring(path.lastIndexOf('/') + 1);
            if (!extracted.isBlank()) {
                return extracted;
            }
        }
        
        return "downloaded_file_" + System.currentTimeMillis();
    }
}
