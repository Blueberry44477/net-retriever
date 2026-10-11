package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Creator for BitTorrent downloaders.
 */
public class TorrentDownloaderProvider extends DownloaderProvider {

    @Override
    public ProtocolDownloader createDownloader() {
        System.out.println("[TorrentDownloaderProvider] [Factory Method] Instantiating TorrentProtocolDownloader");
        return new TorrentProtocolDownloader();
    }
}
