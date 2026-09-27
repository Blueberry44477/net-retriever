package io.github.blueberry44477.net_retriever.downloader;

@FunctionalInterface 
public interface ProgressListener {
    void onProgress(long downloadedBytes);
}
