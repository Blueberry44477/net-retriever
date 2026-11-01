package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Base abstract Decorator conforming to the {@link FileDownloader} Component interface.
 * Maintains a reference to the wrapped {@link FileDownloader} instance and delegates calls by default.
 */
@Getter
@RequiredArgsConstructor
public abstract class FileDownloaderDecorator implements FileDownloader {

    protected final FileDownloader delegate;

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        return delegate.fetchMetadata(source);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        delegate.download(source, destination, startByte, progressListener);
    }

    @Override
    public boolean supports(URI source) {
        return delegate.supports(source);
    }
}
