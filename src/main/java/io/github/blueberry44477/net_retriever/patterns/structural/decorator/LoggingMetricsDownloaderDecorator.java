package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.Getter;

/**
 * Concrete Decorator that collects execution timing and telemetry metrics.
 */
@Getter
public class LoggingMetricsDownloaderDecorator extends FileDownloaderDecorator {

    private long lastDownloadDurationMs;
    private long lastMetadataDurationMs;

    public LoggingMetricsDownloaderDecorator(FileDownloader delegate) {
        super(delegate);
    }

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        long start = System.currentTimeMillis();
        System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-START] fetchMetadata(source=\"%s\")%n", source);
        try {
            DownloadMetadata meta = delegate.fetchMetadata(source);
            this.lastMetadataDurationMs = System.currentTimeMillis() - start;
            System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-SUCCESS] fetchMetadata finished in %d ms (size=%d bytes)%n",
                    lastMetadataDurationMs, meta.getTotalBytes());
            return meta;
        } catch (IOException ex) {
            this.lastMetadataDurationMs = System.currentTimeMillis() - start;
            System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-FAIL] fetchMetadata failed after %d ms: %s%n",
                    lastMetadataDurationMs, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        long start = System.currentTimeMillis();
        System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-START] download(source=\"%s\", dest=\"%s\")%n",
                source, destination);
        try {
            delegate.download(source, destination, startByte, progressListener);
            this.lastDownloadDurationMs = System.currentTimeMillis() - start;
            System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-SUCCESS] download completed in %d ms%n",
                    lastDownloadDurationMs);
        } catch (IOException ex) {
            this.lastDownloadDurationMs = System.currentTimeMillis() - start;
            System.out.printf("[LoggingMetricsDownloaderDecorator] [Stub] [METRICS-FAIL] download failed after %d ms: %s%n",
                    lastDownloadDurationMs, ex.getMessage());
            throw ex;
        }
    }
}
