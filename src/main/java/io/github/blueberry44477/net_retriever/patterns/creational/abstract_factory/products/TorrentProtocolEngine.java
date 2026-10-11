package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;

/**
 * Concrete Product A3: BitTorrent P2P transfer engine.
 */
public class TorrentProtocolEngine implements ProtocolEngine {

    @Override
    public void connect(String endpoint) {
        System.out.println("[TorrentProtocolEngine] [Stub] connect(\"" + endpoint + "\") - Announcing to trackers and resolving DHT swarm peers.");
    }

    @Override
    public void transferData(String destinationPath, long bytesCount) {
        System.out.println("[TorrentProtocolEngine] [Stub] transferData(dest=\"" + destinationPath
                + "\", bytes=" + bytesCount + ") - Downloading piece chunks concurrently across connected swarm peers.");
    }

    @Override
    public void disconnect() {
        System.out.println("[TorrentProtocolEngine] [Stub] disconnect() - Sending stopped event to tracker and closing peer sockets.");
    }
}
