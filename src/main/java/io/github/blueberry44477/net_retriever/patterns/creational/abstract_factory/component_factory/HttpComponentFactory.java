package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpEtagValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpSpeedMonitor;

/**
 * Concrete Factory for the HTTP/HTTPS component family.
 */
public class HttpComponentFactory implements ProtocolComponentFactory {

    @Override
    public ProtocolEngine createEngine() {
        System.out.println("[HttpComponentFactory] [Abstract Factory] createEngine() -> HttpProtocolEngine");
        return new HttpProtocolEngine();
    }

    @Override
    public IntegrityValidator createValidator() {
        System.out.println("[HttpComponentFactory] [Abstract Factory] createValidator() -> HttpEtagValidator");
        return new HttpEtagValidator();
    }

    @Override
    public TransferMonitor createMonitor() {
        System.out.println("[HttpComponentFactory] [Abstract Factory] createMonitor() -> HttpSpeedMonitor");
        return new HttpSpeedMonitor();
    }

    @Override
    public String getFamilyName() {
        return "HTTP/HTTPS Family";
    }
}
