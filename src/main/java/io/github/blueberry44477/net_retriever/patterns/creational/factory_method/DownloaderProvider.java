package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Factory Method pattern: Creator base class declaring the factory method.
 */
public abstract class DownloaderProvider {

    /**
     * Factory Method to be overridden by subclasses.
     */
    protected abstract ProtocolDownloader createDownloader();

    /**
     * Template method executing a download via the product created by the factory method.
     */
    public void processDownload(String url, String destinationPath) {
        System.out.println("[DownloaderProvider] [Template Method] Starting download for: " + url);

        ProtocolDownloader downloader = createDownloader();
        System.out.println("[DownloaderProvider] Instantiated downloader: "
                + downloader.getClass().getSimpleName() + " (" + downloader.getProtocolName() + ")");

        if (downloader.verifySource(url)) {
            downloader.downloadFile(url, destinationPath);
            System.out.println("[DownloaderProvider] Download completed successfully.");
        } else {
            System.err.println("[DownloaderProvider] Error: URL not supported: " + url);
        }
    }
}
