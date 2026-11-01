package io.github.blueberry44477.net_retriever.patterns.structural.proxy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Caching Proxy in the GoF Proxy pattern.
 * Intercepts {@link #fetchMetadata(URI)} requests and caches results to avoid redundant network round-trips.
 */
@Getter
@RequiredArgsConstructor
public class CachingDownloaderProxy implements FileDownloader {

    private final FileDownloader realSubject;
    private final Map<URI, DownloadMetadata> cache = new ConcurrentHashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        DownloadMetadata cached = cache.get(source);
        if (cached != null) {
            this.cacheHits++;
            System.out.printf("[CachingDownloaderProxy] [Stub] Cache HIT for \"%s\" (hits=%d, misses=%d)%n",
                    source, cacheHits, cacheMisses);
            return cached;
        }

        this.cacheMisses++;
        System.out.printf("[CachingDownloaderProxy] [Stub] Cache MISS for \"%s\" - querying RealSubject (hits=%d, misses=%d)%n",
                source, cacheHits, cacheMisses);
        DownloadMetadata metadata = realSubject.fetchMetadata(source);
        cache.put(source, metadata);
        return metadata;
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        System.out.printf("[CachingDownloaderProxy] [Stub] Delegating download to RealSubject: \"%s\"%n", source);
        realSubject.download(source, destination, startByte, progressListener);
    }

    @Override
    public boolean supports(URI source) {
        return realSubject.supports(source);
    }

    public void clearCache() {
        cache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
}
