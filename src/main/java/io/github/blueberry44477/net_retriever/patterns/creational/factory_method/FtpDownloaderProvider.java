package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Creator for FTP downloaders.
 */
public class FtpDownloaderProvider extends DownloaderProvider {

    @Override
    public ProtocolDownloader createDownloader() {
        System.out.println("[FtpDownloaderProvider] [Factory Method] Instantiating FtpProtocolDownloader");
        return new FtpProtocolDownloader();
    }
}
