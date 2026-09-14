package io.github.blueberry44477.net_retriever.service.downloader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public interface FileDownloader {
    DownloadMetadata fetchMetadata(URI source) throws IOException;
    void download(URI source, Path destination, long startByte, ProgressListener progressListener) throws IOException;
    boolean supports(URI source);
}
