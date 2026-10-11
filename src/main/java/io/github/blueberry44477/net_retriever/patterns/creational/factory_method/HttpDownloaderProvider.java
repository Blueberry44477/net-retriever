package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Creator for HTTP downloaders.
 */
public class HttpDownloaderProvider extends DownloaderProvider {

    @Override
    public ProtocolDownloader createDownloader() {
        System.out.println("[HttpDownloaderProvider] [Factory Method] Instantiating HttpProtocolDownloader");
        return new HttpProtocolDownloader();
    }
}
