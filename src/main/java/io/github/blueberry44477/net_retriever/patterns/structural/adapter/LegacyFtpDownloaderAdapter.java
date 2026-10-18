package io.github.blueberry44477.net_retriever.patterns.structural.adapter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.ProgressListener;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Object Adapter adapting {@link LegacyFtpClient} to the standard {@link FileDownloader} interface.
 * Employs object aggregation (composition) instead of class inheritance to satisfy GoF Object Adapter semantics.
 */
@RequiredArgsConstructor
public class LegacyFtpDownloaderAdapter implements FileDownloader {

    @NonNull
    private final LegacyFtpClient ftpClient;

    @Override
    public DownloadMetadata fetchMetadata(URI source) throws IOException {
        System.out.println("[LegacyFtpDownloaderAdapter] [Stub] fetchMetadata(source=\"" + source + "\")");
        if (!supports(source)) {
            throw new IllegalArgumentException("Unsupported URI scheme for FTP adapter: " + source);
        }

        String host = source.getHost() != null ? source.getHost() : "localhost";
        int port = source.getPort() != -1 ? source.getPort() : 21;
        String path = source.getPath();

        ftpClient.connect(host, port, "anonymous");
        long fileSize = ftpClient.getRemoteFileSize(path);
        boolean resumable = ftpClient.isRestartSupported();
        ftpClient.disconnect();

        String fileName = extractFileName(path);
        return new DownloadMetadata(fileSize, resumable, fileName);
    }

    @Override
    public void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException {
        System.out.println("[LegacyFtpDownloaderAdapter] [Stub] download(source=\"" + source
                + "\", destination=\"" + destination + "\", startByte=" + startByte + ")");
        if (!supports(source)) {
            throw new IllegalArgumentException("Unsupported URI scheme for FTP adapter: " + source);
        }

        String host = source.getHost() != null ? source.getHost() : "localhost";
        int port = source.getPort() != -1 ? source.getPort() : 21;
        String path = source.getPath();

        ftpClient.connect(host, port, "anonymous");
        byte[] chunk = ftpClient.retrieveStream(path, startByte, 64 * 1024);
        if (progressListener != null) {
            progressListener.onProgress(startByte + chunk.length);
        }
        ftpClient.disconnect();
    }

    @Override
    public boolean supports(URI source) {
        return source != null && "ftp".equalsIgnoreCase(source.getScheme());
    }

    public LegacyFtpClient getAdaptee() {
        return ftpClient;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "download.bin";
        }
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0 && lastSlash < path.length() - 1) ? path.substring(lastSlash + 1) : path;
    }
}
