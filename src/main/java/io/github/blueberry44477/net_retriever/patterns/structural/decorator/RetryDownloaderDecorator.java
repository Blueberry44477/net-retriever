package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import lombok.Getter;

/**
 * Concrete Decorator providing automatic retry resilience on network failure.
 */
@Getter
public class RetryDownloaderDecorator extends FileDownloaderDecorator {

    private final int maxRetries;
    private final long backoffDelayMs;
    private int attemptCount;

    public RetryDownloaderDecorator(FileDownloader delegate, int maxRetries) {
        this(delegate, maxRetries, 100L);
    }

    public RetryDownloaderDecorator(FileDownloader delegate, int maxRetries, long backoffDelayMs) {
        super(delegate);
        if (maxRetries < 1) {
            throw new IllegalArgumentException("maxRetries must be at least 1: " + maxRetries);
        }
        this.maxRetries = maxRetries;
        this.backoffDelayMs = Math.max(0, backoffDelayMs);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        IOException lastException = null;
        this.attemptCount = 0;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            this.attemptCount = attempt;
            try {
                System.out.printf("[RetryDownloaderDecorator] [Stub] Attempt %d/%d for URI: \"%s\"%n",
                        attempt, maxRetries, source);
                delegate.download(source, destination, startByte, progressListener);
                return;
            } catch (IOException ex) {
                lastException = ex;
                System.out.printf("[RetryDownloaderDecorator] [Stub] Attempt %d failed (%s). Retrying...%n",
                        attempt, ex.getMessage());
                if (attempt < maxRetries && backoffDelayMs > 0) {
                    try {
                        Thread.sleep(backoffDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw lastException != null ? lastException : new IOException("Download failed after " + maxRetries + " attempts");
    }
}
