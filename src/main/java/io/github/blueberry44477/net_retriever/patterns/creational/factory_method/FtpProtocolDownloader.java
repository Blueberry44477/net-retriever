package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Concrete Product: FTP protocol file downloader.
 */
public class FtpProtocolDownloader implements ProtocolDownloader {

    @Override
    public String getProtocolName() {
        return "FTP";
    }

    @Override
    public void downloadFile(String url, String destinationPath) {
        System.out.println("[FtpProtocolDownloader] [Stub] downloadFile(url=\"" + url
                + "\", dest=\"" + destinationPath + "\") - Establishing passive FTP RETR data channel.");
    }

    @Override
    public boolean verifySource(String url) {
        System.out.println("[FtpProtocolDownloader] [Stub] verifySource(url=\"" + url
                + "\") - Sending FTP SIZE request.");
        return url != null && url.startsWith("ftp://");
    }
}
