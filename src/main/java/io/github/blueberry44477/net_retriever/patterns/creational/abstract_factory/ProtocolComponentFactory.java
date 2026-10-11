package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory;

/**
 * Abstract Factory interface: declares creation methods for a compatible family of protocol components.
 */
public interface ProtocolComponentFactory {

    ProtocolEngine createEngine();

    IntegrityValidator createValidator();

    TransferMonitor createMonitor();

    String getFamilyName();
}
