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
import io.github.blueberry44477.net_retriever.service.storage.StoragePathResolver;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DownloadTaskService {
    private final StoragePathResolver pathResolver;
    private final DownloadTaskRepository repository;
    private final List<FileDownloader> downloaders;
    private final ExecutorService executorService;
    private final Map<UUID, Future<?>> activeTasks = new ConcurrentHashMap<>();

    public DownloadTask createTask(URI source, String destination) throws IOException {
        FileDownloader downloader = downloaders.stream()
            .filter(d -> d.supports(source))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported protocol: " + source.getScheme()));

        DownloadMetadata metadata = downloader.fetchMetadata(source);

        String fileName = resolveFileName(source, metadata);
        Path fullDestinationPath = pathResolver.resolve(destination, fileName);

        return saveNewTask(source.toString(), fileName, fullDestinationPath.toString(), metadata.getTotalBytes());
    }

    @Transactional
    protected DownloadTask saveNewTask(String url, String fileName, String savePath, Long totalBytes) {
        DownloadTask task = new DownloadTask(url, fileName, savePath);

        return repository.save(task);
    }

    private String resolveFileName(URI source, DownloadMetadata metadata) {
        if (metadata.getSuggestedFileName() != null && !metadata.getSuggestedFileName().isBlank()) {
            return metadata.getSuggestedFileName();
        }
        
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