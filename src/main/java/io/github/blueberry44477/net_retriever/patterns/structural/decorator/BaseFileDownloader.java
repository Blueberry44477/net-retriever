package io.github.blueberry44477.net_retriever.patterns.structural.decorator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;

/**
 * Concrete Component providing baseline network download operations.
 */
public class BaseFileDownloader implements FileDownloader {

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        System.out.println("[BaseFileDownloader] [Stub] fetchMetadata(source=\"" + source + "\") - Querying remote headers.");
        String filename = extractFilename(source);
        return new DownloadMetadata(25_000_000L, true, filename);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        System.out.println("[BaseFileDownloader] [Stub] download(source=\"" + source
                + "\", dest=\"" + destination + "\", startByte=" + startByte + ") - Streaming network bytes.");
        if (progressListener != null) {
            progressListener.onProgress(startByte + 1024L);
        }
    }

    @Override
    public boolean supports(URI source) {
        if (source == null || source.getScheme() == null) {
            return false;
        }
        String scheme = source.getScheme().toLowerCase();
        return scheme.equals("http") || scheme.equals("https") || scheme.equals("ftp") || scheme.equals("s3");
    }

    private String extractFilename(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "download.bin";
        }
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0 && lastSlash < path.length() - 1) ? path.substring(lastSlash + 1) : path;
    }
}
