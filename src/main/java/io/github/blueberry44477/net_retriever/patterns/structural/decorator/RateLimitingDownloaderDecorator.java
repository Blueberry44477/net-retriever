package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import lombok.Getter;

/**
 * Concrete Decorator that enforces bandwidth rate-limiting / speed throttling on downloads.
 */
@Getter
public class RateLimitingDownloaderDecorator extends FileDownloaderDecorator {

    private final long maxBytesPerSecond;

    public RateLimitingDownloaderDecorator(FileDownloader delegate, long maxBytesPerSecond) {
        super(delegate);
        if (maxBytesPerSecond <= 0) {
            throw new IllegalArgumentException("maxBytesPerSecond must be positive: " + maxBytesPerSecond);
        }
        this.maxBytesPerSecond = maxBytesPerSecond;
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        System.out.printf("[RateLimitingDownloaderDecorator] [Stub] download(source=\"%s\") - Enforcing bandwidth ceiling: %d B/s%n",
                source, maxBytesPerSecond);
        delegate.download(source, destination, startByte, progressListener);
    }
}
