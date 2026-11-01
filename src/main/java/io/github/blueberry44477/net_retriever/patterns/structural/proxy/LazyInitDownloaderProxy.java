package io.github.blueberry44477.net_retriever.patterns.structural.proxy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.RequiredArgsConstructor;

/**
 * Virtual Proxy in the GoF Proxy pattern.
 * Defers creation and connection setup of a heavy {@link FileDownloader} until it is actually requested.
 */
@RequiredArgsConstructor
public class LazyInitDownloaderProxy implements FileDownloader {

    private final Supplier<FileDownloader> subjectSupplier;
    private volatile FileDownloader realSubject;

    private FileDownloader getRealSubject() {
        if (realSubject == null) {
            synchronized (this) {
                if (realSubject == null) {
                    System.out.println("[LazyInitDownloaderProxy] [Stub] [Virtual Proxy] Initializing heavy RealSubject on demand...");
                    this.realSubject = subjectSupplier.get();
                }
            }
        }
        return realSubject;
    }

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        return getRealSubject().fetchMetadata(source);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        getRealSubject().download(source, destination, startByte, progressListener);
    }

    @Override
    public boolean supports(URI source) {
        return getRealSubject().supports(source);
    }

    public boolean isInitialized() {
        return realSubject != null;
    }
}
