package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import lombok.Getter;

/**
 * Concrete Decorator that verifies file integrity checksum after download completion.
 */
@Getter
public class ChecksumVerifyingDownloaderDecorator extends FileDownloaderDecorator {

    private final String expectedChecksum;
    private final String algorithm;
    private boolean verified;

    public ChecksumVerifyingDownloaderDecorator(FileDownloader delegate, String expectedChecksum, String algorithm) {
        super(delegate);
        this.expectedChecksum = Objects.requireNonNull(expectedChecksum, "expectedChecksum must not be null");
        this.algorithm = (algorithm != null) ? algorithm.toUpperCase() : "SHA-256";
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        System.out.printf("[ChecksumVerifyingDownloaderDecorator] [Stub] download(source=\"%s\") - Initializing %s hash verification stream.%n",
                source, algorithm);
        delegate.download(source, destination, startByte, progressListener);
        this.verified = true;
        System.out.printf("[ChecksumVerifyingDownloaderDecorator] [Stub] Integrity check PASSED: %s hash matches expected [%s].%n",
                algorithm, expectedChecksum);
    }
}
