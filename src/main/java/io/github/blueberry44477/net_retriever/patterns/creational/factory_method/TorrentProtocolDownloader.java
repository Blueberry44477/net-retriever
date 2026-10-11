package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Product: BitTorrent P2P protocol file downloader.
 */
public class TorrentProtocolDownloader implements ProtocolDownloader {

    @Override
    public String getProtocolName() {
        return "BitTorrent";
    }

    @Override
    public void downloadFile(String url, String destinationPath) {
        System.out.println("[TorrentProtocolDownloader] [Stub] downloadFile(url=\"" + url
                + "\", dest=\"" + destinationPath + "\") - Resolving peers via DHT and downloading swarm pieces.");
    }

    @Override
    public boolean verifySource(String url) {
        System.out.println("[TorrentProtocolDownloader] [Stub] verifySource(url=\"" + url
                + "\") - Verifying info-hash against BitTorrent tracker.");
        return url != null && (url.startsWith("magnet:?") || url.endsWith(".torrent"));
    }
}
