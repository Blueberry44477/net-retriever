package io.github.blueberry44477.net_retriever.downloadtask;

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

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import io.github.blueberry44477.net_retriever.storage.StoragePathResolver;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DownloadTaskService {
    private final StoragePathResolver pathResolver;
    private final DownloadTaskRepository repository;
    private final List<FileDownloader> downloaders;
    private final ExecutorService executorService;
    private final Map<UUID, Future<?>> activeTasks = new ConcurrentHashMap<>();

    public DownloadTask createTask(String source, String destination) throws IOException {
        URI sourceUri = URI.create(source);
        FileDownloader downloader = downloaders.stream()
                .filter(d -> d.supports(sourceUri))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported protocol: " + sourceUri.getScheme()));

        DownloadMetadata metadata = downloader.fetchMetadata(sourceUri);

        String fileName = resolveFileName(sourceUri, metadata);
        Path fullDestinationPath = pathResolver.resolve(destination, fileName);

        return saveNewTask(source, fileName, fullDestinationPath.toString(), metadata.getTotalBytes());
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