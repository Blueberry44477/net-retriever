package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.FtpProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.FtpSizeValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.FtpStatusMonitor;

/**
 * Concrete Factory for the FTP component family.
 */
public class FtpComponentFactory implements ProtocolComponentFactory {

    @Override
    public ProtocolEngine createEngine() {
        System.out.println("[FtpComponentFactory] [Abstract Factory] createEngine() -> FtpProtocolEngine");
        return new FtpProtocolEngine();
    }

    @Override
    public IntegrityValidator createValidator() {
        System.out.println("[FtpComponentFactory] [Abstract Factory] createValidator() -> FtpSizeValidator");
        return new FtpSizeValidator();
    }

    @Override
    public TransferMonitor createMonitor() {
        System.out.println("[FtpComponentFactory] [Abstract Factory] createMonitor() -> FtpStatusMonitor");
        return new FtpStatusMonitor();
    }

    @Override
    public String getFamilyName() {
        return "FTP Family";
    }
}
