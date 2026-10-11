package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.TorrentPeerMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.TorrentPieceHashValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.TorrentProtocolEngine;

/**
 * Concrete Factory for the BitTorrent component family.
 */
public class TorrentComponentFactory implements ProtocolComponentFactory {

    @Override
    public ProtocolEngine createEngine() {
        System.out.println("[TorrentComponentFactory] [Abstract Factory] createEngine() -> TorrentProtocolEngine");
        return new TorrentProtocolEngine();
    }

    @Override
    public IntegrityValidator createValidator() {
        System.out.println("[TorrentComponentFactory] [Abstract Factory] createValidator() -> TorrentPieceHashValidator");
        return new TorrentPieceHashValidator();
    }

    @Override
    public TransferMonitor createMonitor() {
        System.out.println("[TorrentComponentFactory] [Abstract Factory] createMonitor() -> TorrentPeerMonitor");
        return new TorrentPeerMonitor();
    }

    @Override
    public String getFamilyName() {
        return "BitTorrent Family";
    }
}
