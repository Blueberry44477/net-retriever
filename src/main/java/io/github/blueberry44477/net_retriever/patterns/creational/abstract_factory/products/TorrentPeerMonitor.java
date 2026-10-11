package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;

/**
 * Concrete Product C3: BitTorrent swarm and peer monitor.
 */
public class TorrentPeerMonitor implements TransferMonitor {

    @Override
    public void recordProgress(long transferredBytes, long totalBytes) {
        System.out.printf("[TorrentPeerMonitor] [Stub] recordProgress: %d bytes [Seeds: 42, Peers: 18]%n", transferredBytes);
    }

    @Override
    public void logCompletionSummary() {
        System.out.println("[TorrentPeerMonitor] [Stub] logCompletionSummary: Torrent reached 100%, transition to seeding mode.");
    }
}
