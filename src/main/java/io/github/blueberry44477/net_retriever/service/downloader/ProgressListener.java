package io.github.blueberry44477.net_retriever.service.downloader;

@FunctionalInterface 
public interface ProgressListener {
    void onProgress(long downloadedBytes);
}
